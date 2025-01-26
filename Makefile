# options
CC=gcc
CFLAGS=-W -Wall -pedantic -g
TARGET=chai

# globs
SRCS := $(wildcard src/*.c)
HDRS := $(wildcard src/*.h)
OBJS := $(patsubst src/%.c,bin/%.o,$(SRCS))

# link it all together
$(TARGET): $(OBJS) $(HDRS) Makefile
	@mkdir -p bin
	$(CC) $(CFLAGS) $(OBJS) -o $(TARGET)

# compile an object based on src and headers
bin/%.o: src/%.c $(HDRS) Makefile
	@mkdir -p bin
	$(CC) $(CFLAGS) -c $< -o $@

# tidy up
clean:
	rm -f $(TARGET) $(OBJS)
