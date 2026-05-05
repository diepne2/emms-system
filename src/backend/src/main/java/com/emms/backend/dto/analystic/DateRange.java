package com.emms.backend.dto.analystic;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Khoảng thời gian dùng để filter dữ liệu")
public class DateRange {

    @Schema(description = "Ngày bắt đầu")
    private Date start;

    @Schema(description = "Ngày kết thúc")
    private Date end;


    public DateRange() {
    }

    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }



    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }


    public void validate() {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (start.after(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    @Override
    public String toString() {
        return "DateRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}