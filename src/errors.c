
#include <stdio.h>
#include <stdarg.h>

#include "error.h"

void issue(const char* fname, int line_number, const char* format, ...) {
    // we print errors to stderr with a file name and line number
    fprintf(stderr, "%s:%d: ", fname, line_number);
    va_list args;
    va_start(args, format);
    vfprintf(stderr, format, args);
    va_end(args);
    fprintf(stderr, "\n");
}


