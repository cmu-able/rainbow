/**
 * <p>The <code>parsec</code> project provides a way to perform high-level
 * parsing in which lower-level parsing is delegated into other parsers. It
 * allows building a language out of several parsers which handle different
 * aspects of the language.</p>
 * 
 * <p><code>parsec</code> works by defining very elementary high-level
 * constructs of the language and parsing the level isolating those
 * high-level constructs and then invoking the delegate parsers to handle
 * each one of them.</p>
 * 
 * <p>The language is divided into 2 types of constructs: statements and
 * blocks. A statement can contain any text except for braces and terminates
 * with a semicolon. Blocks can contain any text up to an open brace and its
 * closing brace. Text between braces is not parsed but <code>parsec</code>
 * will look for matching braces.</p>
 * 
 * <p>Delegate parsers are reported whenever a statement of a block is parsed.
 * In general, languages may recurse, if desired, and request further
 * parsing using <code>parsec</code> to handle the contents of blocks. This
 * functionality is not built-in into <code>parsec</code>.</p>
 * 
 * <p>For example, consider the following java language snippet:</p>
 * 
 * <pre>
 * package something;
 * import something_else.*;
 * class my_class {
 *   int x;
 *   void some_method() {
 *     x = 7;
 *   }
 * }
 * </pre>
 * 
 * <p>If the above code snippet is fed into <code>parsec</code>, the delegate
 * parsers will receive 3 notifications: a statement containing text
 * <code>package something</code>, a statement containing text
 * <code>import something_else.*</code> (note that <code>parsec</code> removes
 * the trailing semicolons), and a block containing
 * <code>class my_class { ... }</ code>. The contents of the class are
 * <em>not</em> parsed.</p>
 * 
 * <p>If the contents of the class are parsed, they will yield a statement
 * containing <code>int x</code> and a block containing
 * <code>void some_method() {...}</code>. If the contents of the method are
 * parsed by <code>parsec</code> they will yield a single statement with
 * <code>x = 7</code>.</p>
 * 
 * <p>Delegate parsers, if several parsers are available, <code>parsec</code>
 * will invoke them by the order in which they were added. The statement or
 * block is considered correctly parsed as soon as a parser is able to parse
 * it. Errors from previous delegate parsers are ignored and no further
 * delegate parsers are invoked.</p>
 * 
 * <p>If all delegate parsers fail to parse a statement or block,
 * <code>parsec</code> will report a parsing exception with the error reported
 * by the delegate parser that was able to parse further the statement or
 * block (the one that reports the error later in the text). In case of
 * equality, the error from the first parsers take precedence. When reporting
 * parser errors, <code>parsec</code> will translate the row/column reported
 * by the delegate parsers into row/column numbers of the main text.</p>
 * 
 * <p><code>parsec</code> will also discard comments (single-line and
 * multi line comments with the java syntax) and will also handle double
 * quoted strings properly allowing for <code>\"</code> to be used inside
 * strings to escape double quotes inside strings.</p>
 * 
 * <p><code>parsec</code> is also prepared to handle (although support doesn't
 * yet exist) include statements to build files from multiple parts.</p>
 */
package edu.cmu.cs.able.parsec;
