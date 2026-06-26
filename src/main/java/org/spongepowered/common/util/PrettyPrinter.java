/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.common.util.PrettyPrinter.CentredText;
import org.spongepowered.common.util.PrettyPrinter.Column;
import org.spongepowered.common.util.PrettyPrinter.HorizontalRule;
import org.spongepowered.common.util.PrettyPrinter.ISpecialEntry;
import org.spongepowered.common.util.PrettyPrinter.IVariableWidthEntry;
import org.spongepowered.common.util.PrettyPrinter.KeyValue;
import org.spongepowered.common.util.PrettyPrinter.Row;
import org.spongepowered.common.util.PrettyPrinter.Table;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prints information in a pretty box
 */
public class PrettyPrinter {

    /**
     * Interface for object which supports printing to pretty printer
     */
    public interface IPrettyPrintable {

        /**
         * Append this objec to specified pretty printer
         *
         * @param printer printer to append to
         */
        public abstract void print(PrettyPrinter printer);

    }

    /**
     * Interface for objects which need their width calculated prior to printing
     */
    interface IVariableWidthEntry {

        public abstract int getWidth();

    }

    /**
     * Interface for objects which control their own output format
     */
    interface ISpecialEntry {

    }

    /**
     * A key/value pair for convenient printing
     */
    class KeyValue implements IVariableWidthEntry {

        private final String key;

        private final Object value;

        public KeyValue(final String key, final Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format(PrettyPrinter.this.kvFormat, this.key, this.value);
        }

        @Override
        public int getWidth() {
            return this.toString().length();
        }

    }

    /**
     * Horizontal rule
     */
    class HorizontalRule implements ISpecialEntry {

        private final char[] hrChars;

        public HorizontalRule(final char... hrChars) {
            this.hrChars = hrChars;
        }

        @Override
        public String toString() {
            return new String(this.hrChars).repeat(PrettyPrinter.this.width + 2);
        }

    }

    /**
     * Centred text
     */
    class CentredText {

        private final Object centred;

        public CentredText(final Object centred) {
            this.centred = centred;
        }

        @Override
        public String toString() {
            final String text = this.centred.toString();
            return String.format("%" + (((PrettyPrinter.this.width - (text.length())) / 2) + text.length()) + "s", text);
        }

    }

    /**
     * Table column alignment
     */
    public static enum Alignment {
        LEFT,
        RIGHT
    }

    /**
     * Table information, added to output in order to print header
     */
    static class Table implements IVariableWidthEntry {

        final List<Column> columns = new ArrayList<Column>();

        final List<Row> rows = new ArrayList<Row>();

        String format = "%s";

        int colSpacing = 2;

        boolean addHeader = true;

        void headerAdded() {
            this.addHeader = false;
        }

        void setColSpacing(final int spacing) {
            this.colSpacing = Math.max(0, spacing);
            this.updateFormat();
        }

        Table grow(final int size) {
            while (this.columns.size() < size) {
                this.columns.add(new Column(this));
            }
            this.updateFormat();
            return this;
        }

        Column add(final Column column) {
            this.columns.add(column);
            return column;
        }

        Row add(final Row row) {
            this.rows.add(row);
            return row;
        }

        Column addColumn(final String title) {
            return this.add(new Column(this, title));
        }

        Column addColumn(
            final Alignment align, final int size, final String title) {
            return this.add(new Column(this, align, size, title));
        }

        Row addRow(final Object... args) {
            return this.add(new Row(this, args));
        }

        void updateFormat() {
            final String spacing = " ".repeat(this.colSpacing);
            final StringBuilder format = new StringBuilder();
            boolean addSpacing = false;
            for (final Column column : this.columns) {
                if (addSpacing) {
                    format.append(spacing);
                }
                addSpacing = true;
                format.append(column.getFormat());
            }
            this.format = format.toString();
        }

        String getFormat() {
            return this.format;
        }

        Object[] getTitles() {
            final List<Object> titles = new ArrayList<Object>();
            for (final Column column : this.columns) {
                titles.add(column.getTitle());
            }
            return titles.toArray();
        }

        @Override
        public String toString() {
            boolean nonEmpty = false;
            final String[] titles = new String[this.columns.size()];
            for (int col = 0; col < this.columns.size(); col++) {
                titles[col] = this.columns.get(col).toString();
                nonEmpty |= !titles[col].isEmpty();
            }
            return nonEmpty ? String.format(this.format, (Object[])titles) : null;
        }

        @Override
        public int getWidth() {
            final String str = this.toString();
            return str != null ? str.length() : 0;
        }

    }

    /**
     * Table column, internal
     */
    static class Column {

        private final Table table;

        private Alignment align = Alignment.LEFT;

        private int minWidth = 1;

        private int maxWidth = Integer.MAX_VALUE;

        private int size = 0;

        private String title = "";

        private String format = "%s";

        Column(final Table table) {
            this.table = table;
        }

        Column(final Table table, final String title) {
            this(table);
            this.title = title;
            this.minWidth = title.length();
            this.updateFormat();
        }

        Column(
            final Table table, final Alignment align, final int size, final String title) {
            this(table, title);
            this.align = align;
            this.size = size;
        }

        void setAlignment(final Alignment align) {
            this.align = align;
            this.updateFormat();
        }

        void setWidth(final int width) {
            if (width > this.size) {
                this.size = width;
                this.updateFormat();
            }
        }

        void setMinWidth(final int width) {
            if (width > this.minWidth) {
                this.minWidth = width;
                this.updateFormat();
            }
        }

        void setMaxWidth(final int width) {
            this.size = Math.min(this.size, this.maxWidth);
            this.maxWidth = Math.max(1, width);
            this.updateFormat();
        }

        void setTitle(final String title) {
            this.title = title;
            this.setWidth(title.length());
        }

        private void updateFormat() {
            final int width = Math.min(this.maxWidth, this.size == 0 ? this.minWidth : this.size);
            this.format = "%" + (this.align == Alignment.RIGHT ? "" : "-") + width + "s";
            this.table.updateFormat();
        }

        int getMaxWidth() {
            return this.maxWidth;
        }

        String getTitle() {
            return this.title;
        }

        String getFormat() {
            return this.format;
        }

        @Override
        public String toString() {
            if (this.title.length() > this.maxWidth) {
                return this.title.substring(0, this.maxWidth);
            }

            return this.title;
        }

    }

    /**
     * Table row, internal
     */
    static class Row implements IVariableWidthEntry {

        final Table table;

        final String[] args;

        public Row(final Table table, final Object... args) {
            this.table = table.grow(args.length);
            this.args = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                this.args[i] = args[i].toString();
                this.table.columns.get(i).setMinWidth(this.args[i].length());
            }
        }

        @Override
        public String toString() {
            final Object[] args = new Object[this.table.columns.size()];
            for (int col = 0; col < args.length; col++) {
                final Column column = this.table.columns.get(col);
                if (col >= this.args.length) {
                    args[col] = "";
                } else {
                    args[col] = (this.args[col].length() > column.getMaxWidth()) ? this.args[col].substring(0, column.getMaxWidth()) : this.args[col];
                }
            }

            return String.format(this.table.format, args);
        }

        @Override
        public int getWidth() {
            return this.toString().length();
        }

    }

    /**
     * Minimum allowed width
     */
    private static final int MIN_WIDTH = 40;

    private static final char DEFAULT_RULER = System.getProperty("sponge.prettyPrintingRulerChar", "-").charAt(0);
    private static final boolean INCLUDE_BORDERS = Boolean.parseBoolean(System.getProperty("sponge.prettyPrinting.borders"));
    private static String border() {
        return PrettyPrinter.INCLUDE_BORDERS ? "/*" : "";
    }
    private static String endBorder() {
        return PrettyPrinter.INCLUDE_BORDERS ? "*/" : "";
    }

    /**
     * Horizontal rule
     */
    private final HorizontalRule horizontalRule = new HorizontalRule(PrettyPrinter.DEFAULT_RULER);

    /**
     * Content lines
     */
    private final List<Object> lines = new ArrayList<Object>();

    /**
     * Table
     */
    private Table table;

    /**
     * True when a variable-width entry is added whose width must be calculated
     * on print
     */
    private boolean recalcWidth = false;

    /**
     * Box with (adapts to contents)
     */
    protected int width = 100;

    /**
     *  Wrap width used when an explicit wrap width is not specified
     */
    protected int wrapWidth = 80;

    /**
     * Key/value key width
     */
    protected int kvKeyWidth = 10;

    protected String kvFormat = PrettyPrinter.makeKvFormat(this.kvKeyWidth);

    public PrettyPrinter() {
        this(100);
    }

    public PrettyPrinter(final int width) {
        this.width = Math.max(PrettyPrinter.MIN_WIDTH, width);
        this.wrapWidth = this.width - 20;
    }

    /**
     * Set the wrap width (default 80 columns)
     *
     * @param wrapWidth new width (in characters) to wrap to
     * @return fluent interface
     */
    public PrettyPrinter wrapTo(final int wrapWidth) {
        this.wrapWidth = wrapWidth;
        return this;
    }

    /**
     * Get the current wrap width
     *
     * @return the current wrap width
     */
    public int wrapTo() {
        return this.wrapWidth;
    }

    /**
     * Begin a new table with no header and adaptive column widths
     *
     * @return fluent interface
     */
    public PrettyPrinter table() {
        this.table = new Table();
        return this;
    }

    /**
     * Begin a new table with the specified headers and adaptive column widths
     *
     * @param titles Column titles
     * @return fluent interface
     */
    public PrettyPrinter table(final String... titles) {
        this.table = new Table();
        for (final String title : titles) {
            this.table.addColumn(title);
        }
        return this;
    }

    /**
     * Begin a new table with the specified format. The format is specified as a
     * sequence of values with {@link String}s defining column titles,
     * {@link Integer}s defining column widths, and {@link Alignment}s defining
     * column alignments. Widths and alignment specifiers should follow the
     * relevant column title. Specify a <em>negative</em> value to specify the
     * <em>maximum</em> width for a column (values will be truncated).
     *
     * <p>For example, to specify a table with two columns of width 10:</p>
     *
     * <code>printer.table("Column 1", 10, "Column 2", 10);</code>
     *
     * <p>A table with a column 30 characters wide and a right-aligned column 20
     * characters wide:</p>
     *
     * <code>printer.table("Column 1", 30, "Column 2", 20, Alignment.RIGHT);
     * </code>
     *
     * @param format format string, see description
     * @return fluent interface
     */
    public PrettyPrinter table(final Object... format) {
        this.table = new Table();
        Column column = null;
        for (final Object entry : format) {
            if (entry instanceof String) {
                column = this.table.addColumn((String)entry);
            } else if (entry instanceof Integer && column != null) {
                final int width = ((Integer)entry).intValue();
                if (width > 0) {
                    column.setWidth(width);
                } else if (width < 0) {
                    column.setMaxWidth(-width);
                }
            } else if (entry instanceof Alignment && column != null) {
                column.setAlignment((Alignment)entry);
            } else if (entry != null) {
                column = this.table.addColumn(entry.toString());
            }
        }
        return this;
    }

    /**
     * Set the column spacing for the current table. Default = 2
     *
     * @param spacing Column spacing in characters
     * @return fluent interface
     */
    public PrettyPrinter spacing(final int spacing) {
        if (this.table == null) {
            this.table = new Table();
        }
        this.table.setColSpacing(spacing);
        return this;
    }

    /**
     * Print the current table header. The table header is automatically printed
     * before the first row if not explicitly specified by calling this method.
     *
     * @return fluent interface
     */
    public PrettyPrinter th() {
        return this.th(false);
    }

    private PrettyPrinter th(final boolean onlyIfNeeded) {
        if (this.table == null) {
            this.table = new Table();
        }
        if (!onlyIfNeeded || this.table.addHeader) {
            this.table.headerAdded();
            this.addLine(this.table);
        }
        return this;
    }

    /**
     * Print a table row with the specified values. If more columns are
     * specified than exist in the table, then the table is automatically
     * expanded.
     *
     * @param args column values
     * @return fluent interface
     */
    public PrettyPrinter tr(final Object... args) {
        this.th(true);
        this.addLine(this.table.addRow(args));
        this.recalcWidth = true;
        return this;
    }

    /**
     * Adds a blank line to the output
     *
     * @return fluent interface
     */
    public PrettyPrinter add() {
        this.addLine("");
        return this;
    }

    /**
     * Adds a string line to the output
     *
     * @param string format string
     * @return fluent interface
     */
    public PrettyPrinter add(final String string) {
        this.addLine(string);
        this.width = Math.max(this.width, string.length());
        return this;
    }

    /**
     * Adds a formatted line to the output
     *
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    public PrettyPrinter add(final String format, final Object... args) {
        final String line = String.format(format, args);
        this.addLine(line);
        this.width = Math.max(this.width, line.length());
        return this;
    }

    /**
     * Add elements of the array to the output, one per line
     *
     * @param array Array of objects to print
     * @return fluent interface
     */
    public PrettyPrinter add(final Object[] array) {
        return this.add(array, "%s");
    }

    /**
     * Add elements of the array to the output, one per line
     *
     * @param array Array of objects to print
     * @param format Format for each row
     * @return fluent interface
     */
    public PrettyPrinter add(final Object[] array, final String format) {
        for (final Object element : array) {
            this.add(format, element);
        }

        return this;
    }

    /**
     * Add elements of the array to the output, one per line, with array indices
     *
     * @param array Array of objects to print
     * @return fluent interface
     */
    public PrettyPrinter addIndexed(final Object[] array) {
        final int indexWidth = String.valueOf(array.length - 1).length();
        final String format = "[%" + indexWidth + "d] %s";
        for (int index = 0; index < array.length; index++) {
            this.add(format, index, array[index]);
        }

        return this;
    }

    /**
     * Add elements of the collection to the output, one per line, with indices
     *
     * @param c Collection of objects to print
     * @return fluent interface
     */
    public PrettyPrinter addWithIndices(final Collection<?> c) {
        return this.addIndexed(c.toArray());
    }

    /**
     * Adds a pretty-printable object to the output, the object is responsible
     * for adding its own representation to this printer
     *
     * @param printable object to add
     * @return fluent interface
     */
    public PrettyPrinter add(
        final IPrettyPrintable printable) {
        if (printable != null) {
            printable.print(this);
        }
        return this;
    }

    /**
     * Print a formatted representation of the specified throwable with the
     * default indent (4)
     *
     * @param th Throwable to print
     * @return fluent interface
     */
    public PrettyPrinter add(final Throwable th) {
        return this.add(th, 4);
    }

    /**
     * Print a formatted representation of the specified throwable with the
     * specified indent
     *
     * @param th Throwable to print
     * @param indent Indent size for stacktrace lines
     * @return fluent interface
     */
    public PrettyPrinter add(Throwable th, final int indent) {
        while (th != null) {
            this.add("    %s: %s", th.getClass().getName(), th.getMessage());
            this.add(th.getStackTrace(), indent);
            th = th.getCause();
        }
        return this;
    }

    /**
     * Print a formatted representation of the specified stack trace with the
     * specified indent
     *
     * @param stackTrace stack trace to print
     * @param indent Indent size for stacktrace lines
     * @return fluent interface
     */
    public PrettyPrinter add(final StackTraceElement[] stackTrace, final int indent) {
        final String margin = " ".repeat(indent);
        for (final StackTraceElement st : stackTrace) {
            this.add("%s%s", margin, st);
        }
        return this;
    }

    /**
     * Adds the specified object to the output
     *
     * @param object object to add
     * @return fluent interface
     */
    public PrettyPrinter add(final Object object) {
        return this.add(object, 0);
    }

    /**
     * Adds the specified object to the output
     *
     * @param object object to add
     * @param indent indent amount
     * @return fluent interface
     */
    public PrettyPrinter add(final Object object, final int indent) {
        final String margin = " ".repeat(indent);
        return this.append(object, indent, margin);
    }

    private PrettyPrinter append(final Object object, final int indent, final String margin) {
        if (object instanceof String) {
            return this.add("%s%s", margin, object);
        } else if (object instanceof Iterable) {
            for (final Object entry : (Iterable<?>)object) {
                this.append(entry, indent, margin);
            }
            return this;
        } else if (object instanceof Map) {
            this.kvWidth(indent);
            return this.add((Map<?, ?>)object);
        } else if (object instanceof IPrettyPrintable) {
            return this.add((IPrettyPrintable)object);
        } else if (object instanceof Throwable) {
            return this.add((Throwable)object, indent);
        } else if (object.getClass().isArray()) {
            return this.add((Object[])object, indent + "%s");
        }
        return this.add("%s%s", margin, object);
    }

    /**
     * Adds a formatted line to the output, and attempts to wrap the line
     * content to the current wrap width
     *
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    public PrettyPrinter addWrapped(final String format, final Object... args) {
        return this.addWrapped(this.wrapWidth, format, args);
    }

    /**
     * Adds a formatted line to the output, and attempts to wrap the line
     * content to the specified width
     *
     * @param width wrap width to use for this content
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    public PrettyPrinter addWrapped(final int width, final String format, final Object... args) {
        String indent = "";
        final String line = String.format(format, args).replace("\t", "    ");
        final Matcher indentMatcher = Pattern.compile("^(\\s+)[^\\s]").matcher(line);
        if (indentMatcher.find()) {
            indent = indentMatcher.group(1);
        }

        try {
            for (final String wrappedLine : this.getWrapped(width, line, indent)) {
                this.add(wrappedLine);
            }
        } catch (final Exception ex) {
            this.add(line);
        }
        return this;
    }

    private List<String> getWrapped(final int width, String line, final String indent) {
        final List<String> lines = new ArrayList<String>();

        while (line.length() > width) {
            int wrapPoint = PrettyPrinter.lastBreakIndex(line, width);
            if (wrapPoint < 10) {
                wrapPoint = width;
            }
            final String head = line.substring(0, wrapPoint);
            lines.add(head);
            line = indent + line.substring(wrapPoint + 1);
        }

        if (line.length() > 0) {
            lines.add(line);
        }

        return lines;
    }

    private static int lastBreakIndex(final String line, final int width) {
        final int lineBreakPos = line.lastIndexOf('\n', width);
        return lineBreakPos > -1 ? lineBreakPos : Math.max(line.lastIndexOf(' ', width), line.lastIndexOf('\t', width));
    }

    /**
     * Add a formatted key/value pair to the output
     *
     * @param key Key
     * @param format Value format
     * @param args Value args
     * @return fluent interface
     */
    public PrettyPrinter kv(final String key, final String format, final Object... args) {
        return this.kv(key, String.format(format, args));
    }

    /**
     * Add a key/value pair to the output
     *
     * @param key Key
     * @param value Value
     * @return fluent interface
     */
    public PrettyPrinter kv(final String key, final Object value) {
        this.addLine(new KeyValue(key, value));
        return this.kvWidth(key.length());
    }

    /**
     * Set the minimum key display width
     *
     * @param width width to set
     * @return fluent
     */
    public PrettyPrinter kvWidth(final int width) {
        if (width > this.kvKeyWidth) {
            this.kvKeyWidth = width;
            this.kvFormat = PrettyPrinter.makeKvFormat(width);
        }
        this.recalcWidth = true;
        return this;
    }

    /**
     * Add all values of the specified map to this printer as key/value pairs
     *
     * @param map Map with entries to add
     * @return fluent
     */
    public PrettyPrinter add(final Map<?, ?> map) {
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = entry.getKey() == null ? "null" : entry.getKey().toString();
            this.kv(key, entry.getValue());
        }
        return this;
    }

    /**
     * Adds a horizontal rule to the output
     *
     * @return fluent interface
     */
    public PrettyPrinter hr() {
        return this.hr('*');
    }


    /**
     * Adds a horizontal rule of the specified char to the output
     *
     * @param ruleChar character to use for the horizontal rule
     * @return fluent interface
     */
    public PrettyPrinter hr(final char ruleChar) {
        this.addLine(new HorizontalRule(ruleChar));
        return this;
    }

    /**
     * Centre the last line added
     *
     * @return fluent interface
     */
    public PrettyPrinter centre() {
        if (!this.lines.isEmpty()) {
            final Object lastLine = this.lines.get(this.lines.size() - 1);
            if (lastLine instanceof String) {
                this.addLine(new CentredText(this.lines.remove(this.lines.size() - 1)));
            }
        }
        return this;
    }

    private void addLine(final Object line) {
        if (line == null) {
            return;
        }
        this.lines.add(line);
        this.recalcWidth |= line instanceof IVariableWidthEntry;
    }

    /**
     * Outputs this printer to stderr and to a logger decorated with the calling
     * class name with level {@link Level#DEBUG}
     *
     * @return fluent interface
     */
    public PrettyPrinter trace() {
        return this.trace(PrettyPrinter.getDefaultLoggerName());
    }

    /**
     * Outputs this printer to stderr and to a logger decorated with the calling
     * class name at the specified level
     *
     * @param level Log level to write messages
     * @return fluent interface
     */
    public PrettyPrinter trace(final Level level) {
        return this.trace(PrettyPrinter.getDefaultLoggerName(), level);
    }

    /**
     * Outputs this printer to stderr and to a logger decorated with specified
     * name with level {@link Level#DEBUG}
     *
     * @param logger Logger name to write to
     * @return fluent interface
     */
    public PrettyPrinter trace(final String logger) {
        return this.trace(System.err, LogManager.getLogger(logger));
    }

    /**
     * Outputs this printer to stderr and to a logger decorated with specified
     * name with the specified level
     *
     * @param logger Logger name to write to
     * @param level Log level to write messages
     * @return fluent interface
     */
    public PrettyPrinter trace(final String logger, final Level level) {
        return this.trace(System.err, LogManager.getLogger(logger), level);
    }

    /**
     * Outputs this printer to stderr and to the supplied logger with level
     * {@link Level#DEBUG}
     *
     * @param logger Logger to write to
     * @return fluent interface
     */
    public PrettyPrinter trace(final Logger logger) {
        return this.trace(System.err, logger);
    }

    /**
     * Outputs this printer to stderr and to the supplied logger with the
     * specified level
     *
     * @param logger Logger to write to
     * @param level Log level to write messages
     * @return fluent interface
     */
    public PrettyPrinter trace(final Logger logger, final Level level) {
        return this.trace(System.err, logger, level);
    }

    /**
     * Outputs this printer to the specified stream and to a logger decorated
     * with the calling class name with level {@link Level#DEBUG}
     *
     * @param stream Output stream to print to
     * @return fluent interface
     */
    public PrettyPrinter trace(final PrintStream stream) {
        return this.trace(stream, PrettyPrinter.getDefaultLoggerName());
    }

    /**
     * Outputs this printer to the specified stream and to a logger decorated
     * with the calling class name with the specified level
     *
     * @param stream Output stream to print to
     * @param level Log level to write messages
     * @return fluent interface
     */
    public PrettyPrinter trace(final PrintStream stream, final Level level) {
        return this.trace(stream, PrettyPrinter.getDefaultLoggerName(), level);
    }

    /**
     * Outputs this printer to the specified stream and to a logger with the
     * specified name with level {@link Level#DEBUG}
     *
     * @param stream Output stream to print to
     * @param logger Logger name to write to
     * @return fluent interface
     */
    public PrettyPrinter trace(final PrintStream stream, final String logger) {
        return this.trace(stream, LogManager.getLogger(logger));
    }

    /**
     * Outputs this printer to the specified stream and to a logger with the
     * specified name at the specified level
     *
     * @param stream Output stream to print to
     * @param logger Logger name to write to
     * @param level Log level to write messages
     * @return fluent interface
     */
    public PrettyPrinter trace(final PrintStream stream, final String logger, final Level level) {
        return this.trace(stream, LogManager.getLogger(logger), level);
    }

    /**
     * Outputs this printer to the specified stream and to the supplied logger
     * with level {@link Level#DEBUG}
     *
     * @param stream Output stream to print to
     * @param logger Logger to write to
     * @return fluent interface
     */
    public PrettyPrinter trace(final PrintStream stream, final Logger logger) {
        return this.trace(stream, logger, Level.DEBUG);
    }

    /**
     * Outputs this printer to the specified stream and to the supplied logger
     * with at the specified level
     *
     * @param stream Output stream to print to
     * @param logger Logger to write to
     * @param level Log level to write messages
     * @return fluent interface
     */
    public PrettyPrinter trace(final PrintStream stream, final Logger logger, final Level level) {
        this.log(logger, level);
        this.print(stream);
        return this;
    }

    /**
     * Print this printer to stderr
     *
     * @return fluent interface
     */
    public PrettyPrinter print() {
        return this.print(System.err);
    }

    /**
     * Print this printer to the specified output
     *
     * @param stream stream to print to
     * @return fluent interface
     */
    public PrettyPrinter print(final PrintStream stream) {
        this.updateWidth();
        this.printSpecial(stream, this.horizontalRule);
        for (final Object line : this.lines) {
            if (line instanceof ISpecialEntry) {
                this.printSpecial(stream, (ISpecialEntry)line);
            } else {
                this.printString(stream, line.toString());
            }
        }
        this.printSpecial(stream, this.horizontalRule);
        return this;
    }

    private void printSpecial(final PrintStream stream, final ISpecialEntry line) {
        stream.printf(PrettyPrinter.border() + "%s" + PrettyPrinter.endBorder() + "\n", line.toString());
    }

    private void printString(final PrintStream stream, final String string) {
        if (string != null) {
            stream.printf(PrettyPrinter.border() + " %-" + this.width + "s" + PrettyPrinter.endBorder() + "\n", string);
        }
    }

    /**
     * Write this printer to the specified logger at {@link Level#INFO}
     *
     * @param logger logger to log to
     * @return fluent interface
     */
    public PrettyPrinter log(final Logger logger) {
        return this.log(logger, Level.INFO);
    }

    /**
     * Write this printer to the specified logger at {@link Level#INFO}
     *
     * @param level log level
     * @return fluent interface
     */
    public PrettyPrinter log(final Level level) {
        return this.log(LogManager.getLogger(PrettyPrinter.getDefaultLoggerName()), level);
    }

    /**
     * Write this printer to the specified logger
     *
     * @param logger logger to log to
     * @param level log level
     * @return fluent interface
     */
    public PrettyPrinter log(final Logger logger, final Level level) {
        return this.log(logger, MarkerManager.getMarker(""), level);
    }

    public PrettyPrinter log(final Logger logger, final Marker marker, final Level level) {
        this.updateWidth();
        this.logSpecial(logger, level, marker, this.horizontalRule);
        for (final Object line : this.lines) {
            if (line instanceof ISpecialEntry) {
                this.logSpecial(logger, level, marker, (ISpecialEntry)line);
            } else {
                this.logString(logger, level, marker, line.toString());
            }
        }
        this.logSpecial(logger, level, this.horizontalRule);
        return this;
    }

    private void logSpecial(final Logger logger, final Level level, final ISpecialEntry line) {
        logger.log(level, PrettyPrinter.border() + "{}" + PrettyPrinter.endBorder(), line.toString());
    }

    private void logSpecial(final Logger logger, final Level level, final Marker marker, final ISpecialEntry line) {
        logger.log(level, marker, PrettyPrinter.border() + "{}" + PrettyPrinter.endBorder(), line.toString());
    }

    private void logString(final Logger logger, final Level level, final String line) {
        this.logString(logger, level, MarkerManager.getMarker(""), line);
    }

    private void logString(final Logger logger, final Level level, final Marker marker, final String line) {
        if (line != null) {
            logger.log(level, String.format(PrettyPrinter.border() + " %-" + this.width + "s" + PrettyPrinter.endBorder(), line));
        }
    }

    private void updateWidth() {
        if (this.recalcWidth) {
            this.recalcWidth = false;
            for (final Object line : this.lines) {
                if (line instanceof IVariableWidthEntry) {
                    this.width = Math.min(4096, Math.max(this.width, ((IVariableWidthEntry)line).getWidth()));
                }
            }
        }
    }

    private static String makeKvFormat(final int keyWidth) {
        return String.format("%%%ds : %%s", keyWidth);
    }

    private static String getDefaultLoggerName() {
        final String name = new Throwable().getStackTrace()[2].getClassName();
        final int pos = name.lastIndexOf('.');
        return pos == -1 ? name : name.substring(pos + 1);
    }

    /**
     * Convenience method, alternative to using <tt>Thread.dumpStack</tt> which
     * prints to stderr in pretty-printed format.
     */
    public static void dumpStack() {
        new PrettyPrinter().add(new Exception("Stack trace")).print(System.err);
    }

    /**
     * Convenience methods, pretty-prints the specified throwable to stderr
     *
     * @param th Throwable to log
     */
    public static void print(final Throwable th) {
        new PrettyPrinter().add(th).print(System.err);
    }

}
