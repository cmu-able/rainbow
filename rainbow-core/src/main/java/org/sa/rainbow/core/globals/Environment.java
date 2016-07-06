package org.sa.rainbow.core.globals;

/**
 * States used to track the target deployment environment of Rainbow component.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public enum Environment {
    /** We don't yet know what deployment environment. */
    UNKNOWN,
    /** We're in a Linux environment. */
    LINUX,
    /** We're in a Cygwin environment. */
    CYGWIN,
    /** We're in a Mac FreeBSD environment. */
    MAC,
    /** We're in a Windows environment without Cygwin. */
    WINDOWS
}
