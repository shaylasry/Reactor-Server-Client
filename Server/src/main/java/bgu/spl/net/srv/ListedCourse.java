package bgu.spl.net.srv;

import java.util.TreeSet;

public class ListedCourse {
    private Integer courseIndex;
    private int courseNum;

    public ListedCourse(int courseNum, Integer courseIndex){
        this.courseNum = courseNum;
        this.courseIndex = courseIndex;
    }

    public int getCourseNum() {
        return this.courseNum;
    }

    public Integer getCourseIndex() {
        return this.courseIndex;
    }

    @Override
    public String toString() {
        return String.valueOf(this.courseNum);
    }
}

