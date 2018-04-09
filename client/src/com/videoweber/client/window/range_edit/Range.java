package com.videoweber.client.window.range_edit;

import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Range {

    private final Date begin;
    private final Date end;

    /**
     * 
     * @param begin milliseconds
     * @param end milliseconds
     */
    public Range(Date begin, Date end) {
        if (begin == null || end == null) {
            throw new NullPointerException();
        }
        if (begin.after(end)) {
            throw new IllegalArgumentException("Begin couldn't be after end.");
        }
        if (end.compareTo(begin) == 0) {
            throw new IllegalArgumentException("Range couldn't be 0.");
        }
        this.begin = begin;
        this.end = end;
    }

    public Date getBegin() {
        return begin;
    }

    public Date getEnd() {
        return end;
    }

    public Long getDuration() {
        return end.getTime() - begin.getTime();
    }

}
