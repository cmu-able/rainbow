#!/usr/bin/env python
# Software License Agreement (BSD License)
#
# Copyright (c) 2008, Willow Garage, Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#  * Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#  * Redistributions in binary form must reproduce the above
#    copyright notice, this list of conditions and the following
#    disclaimer in the documentation and/or other materials provided
#    with the distribution.
#  * Neither the name of Willow Garage, Inc. nor the names of its
#    contributors may be used to endorse or promote products derived
#    from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
# FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#
# Revision $Id$
#
# Adapted from standard ros "rostopic echo" command
# TODO: change to track multiple topics
# TODO: change to report on a period
# TODO: make parameterizable by topic
# TODO: read in YAML file that specifies probes

from __future__ import division, print_function
from threading import Lock

NAME = 'rosprobe'

import sys
import socket
import time
import traceback
try:
    from xmlrpc.client import Fault
except ImportError:
    from xmlrpclib import Fault

from operator import itemgetter
try:
    from urllib.parse import urlparse
except ImportError:
    from urlparse import urlparse

import genpy
import roslib.message
import rosgraph
import rospy

output_lock = Lock()

class ROSTopicException(Exception):
    """
    Base exception class for rostopic-related errors
    """
    pass

class ROSTopicIOException(ROSTopicException):
    """
    rostopic errors related to network I/O failures
    """
    pass

def _check_master():
    """
    Makes sure that the master is available
    :raises :exc:`RosTopicException` If unable to successfully communicate with master
    """
    try:
        rosgraph.Master('/rostopic').getPid()
    except socket.error:
        raise ROSTopicIOException("Unable to communicate with master!")

def _master_get_topic_types(master):
    try:
        val = master.getTopicTypes()
    except Fault:
        sys.stderr.write("WARNING: rostopic is being used against an older version of ROS/roscore\n")
        val = master.getPublishedTopics('/')
    return val

class CallbackEcho(object):
    """
    Callback instance that can print callback data in a variety of
    formats. Used for all variants of rostopic echo
    """

    def __init__(self, topic, msg_eval, count=None, period=None):
        if topic and topic[-1] == '/':
            topic = topic[:-1]
        self.topic = topic
        self.msg_eval = msg_eval
        self.prefix = ''
        self.suffix = '\n---'


        # done trackes when we've exceeded the count
        self.done = False
        self.max_count = count
        self.count = 0
        self.str_fn = self.custom_strify_message

        self.period = period
        self.last_report = 0.0

        # first tracks whether or not we've printed anything yet.
        # Need this for printing plot fields.
        self.first = True

        # cache
        self.last_topic = None
        self.last_msg_eval = None

    def custom_strify_message(self, val, indent='', time_offset=None, current_time=None, type_information=None):
        # ensure to print uint8[] as array of numbers instead of string
        if type_information and type_information.startswith('uint8['):
            val = [ord(x) for x in val]
        return genpy.message.strify_message(val, indent=indent, time_offset=time_offset, current_time=current_time, field_filter=None, fixed_numeric_width=None)

    def callback(self, data, callback_args, current_time=None):
        """
        Callback to pass to rospy.Subscriber or to call
        manually. rospy.Subscriber constructor must also pass in the
        topic name as an additional arg
        :param data: Message
        :param topic: topic name, ``str``
        :param current_time: override calculation of current time, :class:`genpy.Time`
        """
        # Check the periodicity of this report
        #sys.stdout.write("Got message\n")
        #sys.stdout.flush()
        if self.period is not None:
            datetime = rospy.get_rostime().to_sec()
            #sys.stdout.write (str(self.last_report) + "\n")
            #sys.stdout.write (str(datetime - self.last_report) + " < " + str(self.period))
            #sys.stdout.flush()
            if self.last_report != 0.0:
                if (datetime - self.last_report) < self.period:
                    #sys.stdout.write("not reporttttttttttttttttttttttttttttin")
                    return

            self.last_report = datetime
            #sys.stdout.write("Time to report\n")
            sys.stdout.flush()
        topic = callback_args['topic']
        type_information = callback_args.get('type_information', None)
        if self.max_count is not None and self.count >= self.max_count:
            self.done = True
            return

        try:
            msg_eval = self.msg_eval
            if topic == self.topic:
                pass
            elif self.topic.startswith(topic+'/'):
                # self.topic is actually a reference to a topic field, generate msgeval
                if topic == self.last_topic:
                    # use cached eval
                    msg_eval = self.last_msg_eval
                else:
                    # generate msg_eval and cache
                    self.last_msg_eval = msg_eval = msgevalgen(self.topic[len(topic):])
                    self.last_topic = topic

            if msg_eval is not None:
                data = msg_eval(data)

            # data can be None if msg_eval returns None
            if data is not None:
                # NOTE: we do all the prints using direct writes to sys.stdout, which works better with piping
                try:
                    output_lock.acquire()
                    self.count += 1
                    sys.stdout.write(self.prefix+\
                                "topic: " + self.topic + "\n"+\
                                self.str_fn(data, current_time=current_time, type_information=type_information) + \
                                self.suffix + "\n")
                    sys.stdout.flush()
                finally:
                    output_lock.release()
            if self.max_count is not None and self.count >= self.max_count:
                self.done = True
        except IOError:
            self.done = True
        except:
            self.done = True
            traceback.print_exc()

def _sleep(duration):
    rospy.rostime.wallsleep(duration)

def msgevalgen(pattern):
    """
    Generates a function that returns the relevant field(s) (aka 'subtopic(s)') of a Message object
    :param pattern: subtopic, e.g. /x[2:]/y[:-1]/z, ``str``
    :returns: function that converts a message into the desired value, ``fn(Message) -> value``
    """
    evals = [] # list of (field_name, slice_object) pairs
    fields = [f for f in pattern.split('/') if f]
    for f in fields:
        if '[' in f:
            field_name, rest = f.split('[', 1)
            if not rest.endswith(']'):
                print("msssing closing ']' in slice spec '%s'" %f, file=sys.stderr)
                return None
            rest = rest[:-1] # slice content, removing closing bracket
            try:
                array_index_or_slice_object = _get_array_index_or_slice_object(rest)
            except AssertionError as e:
                print ("field '%s' has invalid slice argument '%s': %s" % (field_name, rest, str(e)), file=sys.stderr)
                return None
            evals.append((field_name, array_index_or_slice_object))
        else:
            evals.append((f, None))

    def msgeval(msg, evals):
        for i, (field_name, slice_object) in enumerate(evals):
            try: # access field first
                msg = getattr(msg, field_name)
            except AttributeError:
                print("no field named %s in %s" % (field_name, pattern), file=sys.stderr)
                return None
            if slice_object is not None: # access slide
                try:
                    msg = msg.__getitem__(slice_object)
                except IndexError as e:
                    print("%s: %s" % (str(e), pattern), file=sys.stderr)
                    return None

                # if a list is returned her (i.e. not only a single element accessed),
                # we need to recursively call msg_eval() with the rest of evals
                # in order to handle nested slices
                if isinstance(msg, list):
                    rest = evals[i+1]
                    return [msgeval(m, rest) for m in msg]
        return msg

    return (lambda msg: msgeval(msg, evals)) if evals else None

def _get_array_index_or_slice_object(index_string):
    assert index_string != '', 'empty array index'
    index_string_parts = index_string.split(':')
    if len(index_string_parts) == 1:
        try:
            array_index = int(index_string_parts[0])
        except ValueError:
            assert False, "non-integer array index step '%s'" % index_string_parts[0]
        return array_index

    slice_args = [None, None, None]
    if index_string_parts[0] != '':
        try:
            slice_args[0] = int(index_string_parts[0])
        except ValueError:
            assert False, "non-integer slice start '%s'" % index_string_parts[0]
    if index_string_parts[1] != '':
        try:
            slice_args[1] = int(index_string_parts[1])
        except ValueError:
            assert False, "non-integer slice stop '%s'" % index_string_parts[1]
    if len(index_string_parts) > 2 and index_string_parts[2] != '':
        try:
            slice_args[2] = int(index_string_parts[2])
        except ValueError:
            assert False, "non-integer slice step '%s'" % index_string_parts[2]
    if len(index_string_parts) > 3:
        assert False, 'too many slice arguments'
    return slice(*slice_args)

def _get_nested_attribute(msg, nested_attributes):
    value = msg
    for attr in nested_attributes.split('/'):
        value = getattr(value, attr)
    return value

def _get_topic_type(topic):
    """
    subroutine for getting the topic type
    :returns: topic type, real topic name and fn to evaluate the message instance
    if the topic points to a field within a topic, e.g. /rosout/msg, ``(str, str, fn)``
    """
    try:
        val = _master_get_topic_types(rosgraph.Master('/rostopic'))
    except socket.error:
        raise ROSTopicIOException("Unable to communicate with master!")

    # exact match first, followed by prefix match
    matches = [(t, t_type) for t, t_type in val if t == topic]
    if not matches:
        matches = [(t, t_type) for t, t_type in val if topic.startswith(t+'/')]
        # choose longest match
        matches.sort(key=itemgetter(0), reverse=True)

        # try to ignore messages that don't have the field specified as part of the topic name
        while matches:
            t, t_type = matches[0]
            msg_class = roslib.message.get_message_class(t_type)
            if not msg_class:
                # if any class is not fetchable skip ignorng any messages
                break
            msg = msg_class()
            nested_attributes = topic[len(t) + 1:].rstrip('/')
            nested_attributes = nested_attributes.split('[')[0]
            if nested_attributes == '':
                break
            try:
                _get_nested_attribute(msg, nested_attributes)
            except AttributeError:
                # ignore this type since it does not have the requested field
                matches.pop(0)
                continue
            matches = [(t, t_type)]
            break

    if matches:
        t, t_type = matches[0]
        if t_type == rosgraph.names.ANYTYPE:
            return None, None, None
        return t_type, t, msgevalgen(topic[len(t):])
    else:
        return None, None, None

def get_topic_type(topic, blocking=False):
    """
    Get the topic type.

    :param topic: topic name, ``str``
    :param blocking: (default False) block until topic becomes available, ``bool``
    :returns: topic type, real topic name and fn to evaluate the message instance
        if the topic points to a field within a topic, e.g. /rosout/msg. fn is None    otherwise. ``(str, str, fn)``
    :raises: :exc:`ROSTopicException` If master cannot be contacted
    """
    topic_type, real_topic, msg_eval = _get_topic_type(topic)
    if topic_type:
        return topic_type, real_topic, msg_eval
    elif blocking:
        sys.stderr.write("WARNING: topic [%s] does not appear to be published yet\n"% topic)
        while not rospy.is_shutdown():
            topic_type, real_topic, msg_eval = _get_topic_type(topic)
            if topic_type:
                return topic_type, real_topic, msg_eval
            else:
                _sleep(0.1)
    return None, None, None

def get_topic_class(topic, blocking=False):
    """
    Get the topic message class
    :returns: message class for topic, real topic
      name, and function for evaluating message objects into the subtopic
      (or ``None``). ``(Message, str, str)``
    :raises: :exc:`ROSTopicException` If topic type cannot be determined or loaded
    """
    topic_type, real_topic, msg_eval = get_topic_type(topic, blocking=blocking)
    if topic_type is None:
        return None, None, None
    msg_class = roslib.message.get_message_class(topic_type)
    if not msg_class:
        raise ROSTopicException("Cannot load message class for [%s]. Are your messages built?" % topic_type)
    return msg_class, real_topic, msg_eval

def has_zero_counts(callbacks):
    for callback in callbacks:
        if callback.count == 0:
            return True
    return False

def all_done(callbacks):
    for callback in callbacks:
        if not callback.done:
            return False
    return True

def warn_no_callbacks(callbacks):
    for callback in callbacks:
        if callback.count == 0 and not callback.done:
            sys.stderr.write("WARNING: no messages received on '%s' and simulated time is active.\n is /clock being published?\n" % callback.topic)

def _rostopic_echo(topics, callbacks):
    """
    Print new messages on topic to screen.

    :param topics: topic name, ``str``
    :param callbacks: the callbacks for managing subscriptions 
    Each list should be the same length
    """
    # we have to init a node regardless and bag echoing can print timestamps
    _check_master()
    rospy.init_node(NAME, anonymous=True)
    # iterate over all the topics and callbacks 
    for index in range(len(topics)):
        topic = topics[index]
        callback_echo = callbacks[index]
        msg_class, real_topic, msg_eval = get_topic_class(topic, blocking=True)
        if msg_class is None:
            continue
        callback_echo.msg_eval = msg_eval

        # extract type information for submessages
        type_information = None
        if len(topic) > len(real_topic):
            subtopic = topic[len(real_topic):]
            subtopic = subtopic.strip('/')
            submsg_class = msg_class
            if subtopic:
                fields = subtopic.split('/')
                submsg_class = msg_class
                while fields:
                    field = fields[0].split('[')[0]
                    del fields[0]
                    index = submsg_class.__slots__.index(field)
                    type_information = submsg_class._slot_types[index]
                    if fields:
                        submsg_class = roslib.message.get_message_class(type_information.split('[', 1)[0])
                        if not submsg_class:
                            raise ROSTopicException("Cannot load message class for [%s]. Are your messages built?" % type_information)

        
        sub = rospy.Subscriber(real_topic, msg_class, callback_echo.callback, {'topic' : topic, 'type_information' : type_information})

    use_sim_time = rospy.get_param("/use_sim_time", False)
    if use_sim_time:
        timeout_t = time.time() + 2
        while time.time() < timeout_t and \
                not rospy.is_shutdown() and \
                has_zero_counts(callbacks) and \
                not all_done(callbacks):
            _sleep(0.1)

        if has_zero_counts(callbacks) and \
                not rospy.is_shutdown() and \
                not all_done(callbacks):
            warn_no_callbacks(callbacks)

    while not rospy.is_shutdown() and not all_done(callbacks):
        _sleep(0.1)


def _rostopic_cmd_echo(argv):
    def expr_eval(expr):
        def eval_fn(m):
            return eval(expr)
        return eval_fn

    args = argv # not passing in 'echo' as command because this is the default
    from optparse import OptionParser
    parser = OptionParser(usage="usage: %prog [options] /topic[:period (secs)]", prog=NAME)
    parser.add_option("-n",
                    dest="msg_count", default=None, metavar="COUNT",
                    help="number of messages to echo")
    (options, args) = parser.parse_args(args)
    if len(args) == 0:
        parser.error("topic must be specified")
    try:
        msg_count = int(options.msg_count) if options.msg_count else None
    except ValueError:
        parser.error("COUNT must be an integer")
    callbacks = []
    topics = []
    for t in args:
        t_p = t.split(':')
        topic = rosgraph.names.script_resolve_name('rostopic', t_p[0])
        if len(t_p) > 1:
            try:
                period = int(t_p[1])
            except ValueError:
                parser.error("period must be an integer")
        else:
            period = None
        callbacks.append(CallbackEcho(topic, None, count=msg_count, period=period))
        topics.append(topic)
        #callback_echo = CallbackEcho(topic, None, count=msg_count)
    try:
        _rostopic_echo(topics, callbacks)
    except socket.error:
        sys.stderr.write("Network communication failed. Most likely failed to communicate with master.\n")

def rosprobemain(argv=None):
    if argv is None:
        argv = sys.argv

    # Filter out remapping arguments in case we are being invoked via roslaunch
    argv = rospy.myargv(argv)

    try:
        _rostopic_cmd_echo(argv)
    except socket.error:
        sys.stderr.write("Network communication failed. Most likely failed to communicate with master.\n")
        sys.exit(1)
    except ROSTopicException as e:
        sys.stderr.write("ERROR: %s\n"%str(e))
        sys.exit(1)
    except KeyboardInterrupt: pass
    except rospy.ROSInterruptException: pass

if __name__ == '__main__':
    rosprobemain(sys.argv[1:])
