package me.utk.spigot_scripting.script;

import me.utk.spigot_scripting.util.exception.ScanningException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A general purpose scanner.
 * Definitely NOT thread-safe.
 *
 * @author Utkarsh Priyam
 * @version 11/23/20
 */
public class Scanner {
    private final BufferedReader bf;

    private char[] nextLine = {};
    private int index = 0, lineNum = 0;

    private long numTokens = 0L;
    public long tokensPassed() {
        return numTokens;
    }

    private char currentChar = '\0';
    private Token currentToken = null;
    private Token lookAheadToken;

    private boolean eof = false;
    public boolean hasMore() {
        return !eof;
    }

    public Scanner(InputStream source) {
        this(source, true);
    }
    public Scanner(InputStream source, boolean advanceFirstToken) {
        bf = new BufferedReader(new InputStreamReader(source));

        nextChar();
        lookAheadToken = advanceInternally();
        if (advanceFirstToken)
            advance();
    }

    private char nextChar() {
        try {
            if (index >= nextLine.length) {
                String temp = bf.readLine();
                if (temp == null)
                    return currentChar = '\0';
                nextLine = (temp + '\n').toCharArray();
                lineNum++;
                index = 0;
            }
            return currentChar = nextLine[index++];
        } catch (IOException e) {
            throw new ScanningException("Unexpected error while parsing file");
        }
    }

    public Token current() {
        return currentToken;
    }
    public Token advance() {
        if (eof)
            return currentToken = lookAheadToken;

        numTokens++;
        currentToken = lookAheadToken;
        lookAheadToken = advanceInternally();
        eof = lookAheadToken.token == null;
        return currentToken;
    }

    private boolean isSingleChar(char c) {
        return c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' || c == ';' || c == ',' || c == '\0';
    }
    @SuppressWarnings("StatementWithEmptyBody")
    private Token advanceInternally() {
        StringBuilder builder = new StringBuilder();
        int r, c;

        if (currentChar == '\0')
            return new Token(null, lineNum, index);

        // Skip/Parse whitespace and comments
        while (isWhiteSpace(currentChar) || currentChar == '/')
            if (isWhiteSpace(currentChar))
                while (isWhiteSpace(nextChar())) /* Do Nothing */ ;
            else /* if (currentChar == '/') */ {
                r = lineNum;
                c = index;
                if (nextChar() == '/')
                    while (nextChar() != '\n') /* Do Nothing */ ;
                else {
                    builder.append('/');
                    while (!isWhiteSpace(nextChar()) && !isSingleChar(currentChar))
                        builder.append(currentChar);
                    return new Token(builder.toString(), r, c);
                }
            }

        if (isSingleChar(currentChar)) {
            if (currentChar == '\0')
                return new Token(null, lineNum, index);
            char oldChar = currentChar;
            nextChar();
            return new Token("" + oldChar, lineNum, index);
        }



        r = lineNum;
        c = index;
        builder.append(currentChar);
        while (!isWhiteSpace(nextChar()) && !isSingleChar(currentChar))
            builder.append(currentChar);
        return new Token(builder.toString(), r, c);
    }

    private boolean isWhiteSpace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    @Override
    public String toString() {
        return lineNum + " " + index;
    }

    public static class Token {
        public final String token;

        public final int row, col;

        private Token(String token, int r, int c) {
            this.token = token;

            this.row = r;
            this.col = c;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(Line ").append(row).append(" Column ").append(col).append(")");
            for (int i = 25 - builder.length(); i > 0; i--)
                builder.append(" ");
            return builder.append(token).toString();
        }
    }
}
