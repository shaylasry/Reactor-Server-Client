package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentSkipListSet;

public class Course {
    private Integer index;
    private int courseNum;
    private String courseName;
    private int [] kdamCourseList;
    private int numOfMaxStudents;
    int numberOfSeatsAvailable;
    private ConcurrentSkipListSet<String> studentList;

    public Course(int courseNum, String courseName, int [] kdamCourseList, int numOfMaxStudents,Integer courseSpot){
        this.courseNum = courseNum;
        this.courseName = courseName;
        this.numOfMaxStudents = numOfMaxStudents;
        this.kdamCourseList = kdamCourseList;
        this.numberOfSeatsAvailable = numOfMaxStudents;
        this.studentList = new ConcurrentSkipListSet<String>();
        this.index = courseSpot;
    }

    public int getCourseNum() {
        return courseNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public int [] getKdamCourseList() {
        return kdamCourseList;
    }

    public int getNumOfMaxStudents() {
        return numOfMaxStudents;
    }

    public int getNumberOfSeatsAvailable() { return numberOfSeatsAvailable; }

    public Integer getIndex() {
        return index;
    }

    public ConcurrentSkipListSet getStudentList() {
        return this.studentList;
    }


    public void addStudent(String user){
        if (numberOfSeatsAvailable > 0) {
            this.studentList.add(user);
            this.numberOfSeatsAvailable--;
        }
    }

    public boolean removeStudent(String user){
        boolean removed = this.studentList.remove(user);
        if(removed)
            this.numberOfSeatsAvailable++;
        return removed;
    }

    public boolean isAvailable(){
        return (this.numberOfSeatsAvailable != 0);
    }


    @Override
    public String toString() {
        return
                "" + courseNum +
                        "|" + courseName +
                        "|" + numberOfSeatsAvailable +
                        "|" + numOfMaxStudents +
                        "|" + studentList;
    }


}