# Performs a test and exits if it failes. Arguments are the test.
function ensure {
  echo "Asserting '$@'"
  [ "$@" ] || {
    echo "  test failed"
    exit 1
  }
  echo "  test ok"
}

# Runs a command and reports it to the user
function runcmd {
  if [[ "$1" =~ ^:([[:alnum:]_]+)$ ]]; then
    RECEIVE_VARIABLE=${BASH_REMATCH[1]}
    shift;
  else
    RECEIVE_VARIABLE=""
  fi

  if [ -n "$RECEIVE_VARIABLE" ]; then
    echo "Executing (output captured) $@"
    eval $RECEIVE_VARIABLE=$("$@") || exit 1
  else
    echo "Executing $@"
    "$@" || exit 1
  fi
}

# Counts the number of lines in a file that optionally match a regular
# expression. First argument is variable name that will receive the count.
# Second the file and third is the optional regular expression
function count_file_lines {
  NAME="$1"
  FILE="$2"
  REGEX="$3"

  echo "Counting lines of '$FILE'"
  ensure -r "$FILE"
  if [ -a "$REGEX" ]; then
    REGEX=".*"
  fi

  runcmd :$NAME bash -lc "cat '$FILE' | egrep '$REGEX' | wc -l"
}

# Replaces / by \/ in a regex
function protect_regex {
  echo "$1" | $ZNN_SEDRE 's/\//\\\//g'
}

# Overwrites a file with another preserving the latters's execution bit.
function overwrite_file {
  SRC="$1"
  DST="$2"

  ensure -f "$SRC"
  ensure -f "$DST"
  if [ -x "$DST" ]; then
    IS_EXEC=1
  else
    IS_EXEC=0
  fi

  mv "$SRC" "$DST"
  if [ "$IS_EXEC" == "1" ]; then
    chmod 755 "$DST"
  fi
}
# Replace a single line in a file. The first argument is the file. The second argument is the regular expression that matches the file. The third is the replace string
function replace_line_regex {
  FILE="$1"
  REGEX=$(protect_regex "$2")
  REPL=$(protect_regex "$3")
  ensure -f "$FILE"
  count_file_lines COUNT "$FILE" "$REGEX"
  ensure "$COUNT" == "1"
  RFILE=$(mktemp)
  bash -lc "cat '$FILE' | $ZNN_SEDRE 's/$REGEX/$REPL/' > $RFILE"
  overwrite_file "$RFILE" "$FILE"
}
 
# Finds a line number in a file that matches a regular expression. Receives
# the variable name to put the line number in, file, and regular expression
function find_line {
  FIND_LINE_VAR_NAME="$1"
  FILE="$2"
  REGEX=$(protect_regex "$3")
  echo "Finding line in '$FILE' that matches /$REGEX/"
  ensure -r "$FILE"
  count_file_lines COUNT "$FILE" "$REGEX"
  if [ "$COUNT" -ne "1" ]; then
    echo "Exactly 1 line expected to match but $COUNT found."
    exit 1
  fi
  runcmd :FOUND $ZNN_SEDNRE "/$REGEX/=" "$FILE"
  echo "Line found is $FOUND"
  ensure $FOUND -ge 1
  eval $FIND_LINE_VAR_NAME=$FOUND
}

# Finds the first line number in a file that matches a regular expression, if
# any. Receives the variable name to put the line number (receives -1 if no
# line was found), the file, the line to start at (1 is the beginning of the 
# file) and the regular expression
function find_first_line {
  NAME="$1"
  FILE="$2"
  START="$3"
  REGEX=$(protect_regex "$4")

  echo "Finding first line in '$FILE' after $START that matches /$REGEX/." 
  ensure -r "$FILE"
  LINES=($($ZNN_SEDNRE "/$REGEX/=" "$FILE")) || exit 1
  echo "  Found ${#LINES[@]} lines matching"
  FIRST_LINE="-1"
  for L in "${LINES[@]}"; do
    if [ "$L" -gt "$START" -a "$FIRST_LINE" -eq "-1" ]; then
      FIRST_LINE="$L"
    fi
  done
  echo "FIrst line matching is $FIRST_LINE."
  eval $NAME=$FIRST_LINE || exit 1
}

# Inserts a line in a file at a given line number. All lines are pushed back.
# Receives the file, line number and line to add. A newline is automaticall
# added at the end of the line to add.
function insert_line {
  FILE="$1"
  LINE_NUMBER="$2"
  TEXT="$3"

  echo "Adding '$TEXT' after line '$LINE_NUMBER' in file '$FILE'"
  ensure -r "$FILE"
  RFILE=$(mktemp)
  runcmd bash -lc \
    "sed \"${LINE_NUMBER}a $TEXT\" \"$FILE\" > \"$RFILE\""
  overwrite_file "$RFILE" "$FILE"
}
