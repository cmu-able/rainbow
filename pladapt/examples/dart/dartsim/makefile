CXXFLAGS=-std=c++11 -O3 -Wall -fmessage-length=0 -g

OBJS=main.o Simulation.o
LDLIBS=-ldartam
TARGET=dartsim
CPPFLAGS=-I../dartam/include -I../../../include
LDFLAGS=-L../dartam/build/src/.libs

all: $(TARGET)

debug: CXXFLAGS += -g
debug: $(TARGET)

$(TARGET): $(OBJS)
	$(CXX) -o $(TARGET) $(OBJS) $(LDFLAGS) $(LDLIBS)

clean:
	 rm -f $(OBJS) $(TARGET)
