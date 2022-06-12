package bgu.spl.net.impl.BGRSServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.command.*;
import bgu.spl.net.command.Error;

import bgu.spl.net.srv.Course;
import bgu.spl.net.srv.ListedCourse;


import java.io.*;
import java.util.concurrent.ConcurrentSkipListSet;



/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
	private static Database singleton =null;
	ConcurrentHashMap<Integer, Course> courses;
	ConcurrentHashMap<String,Boolean> userLog;
	ConcurrentHashMap<String,Boolean> userType;
	ConcurrentHashMap<String,ConcurrentSkipListSet<ListedCourse>> studentData;
	ConcurrentHashMap<String,String> admin;
	ConcurrentHashMap<String,String> student;

	private Database() {
		// TODO: implement
		courses = new ConcurrentHashMap<>();
		studentData = new ConcurrentHashMap<>();
		admin = new ConcurrentHashMap<>();
		student = new ConcurrentHashMap<>();
		userLog = new ConcurrentHashMap<String,Boolean>();
		userType = new ConcurrentHashMap<String,Boolean>();
	}


	/**
	 * Retrieves the single instance of this class.
	 */
	public static Database getInstance() {
		if(singleton == null){
			singleton = new Database();
			singleton.initialize("./Courses.txt");
		}
		return singleton;
	}

	/**
	 * loades the courses from the file path specified
	 * into the Database, returns true if successful.
	 */
	boolean initialize(String coursesFilePath) {
		// TODO: implement
		try(BufferedReader reader= new BufferedReader(new FileReader(coursesFilePath))){
			String line;
			Integer courseIndex = 0;
			while((line = reader.readLine())!=null){
				String[] temp = line.split("\\|");
				int [] kdamCourse={};
				if(!(temp[2].equals("[]"))) {
					String[] tepmKdamCourses = temp[2].substring(1, temp[2].length() -1).split(",");
					kdamCourse = new int [tepmKdamCourses.length];
					for (int i = 0; i < kdamCourse.length; i++) {
						kdamCourse[i] = Integer.parseInt(tepmKdamCourses[i]);
					}
				}
				this.courses.put(Integer.parseInt(temp[0]),(new Course(Integer.parseInt(temp[0]),temp[1],kdamCourse,Integer.parseInt(temp[3]),courseIndex)));
				courseIndex++;
			}


		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	//1
	public Command adminReg(boolean isAdmin,String userName,String password){
		//checks if user name and password are valid and if the one who tries to register is an admin or a student
		if(!isAdmin || userName.length()==0 || password.length()==0) {
			return new Error("13|1");
		}
		if (admin.containsKey(userName)){
			return new Error("13|1");
		}
		//use index[0] to know if logged in and index[1] if admin/student
		userLog.put(userName,false);
		userType.put(userName,true);
		admin.put(userName,password);
		return new Ack("12|1");
	}

	//2
	public Command studentReg(boolean isAdmin,String userName,String password){
		//checks if user name and password are valid and if the one who tries to register is an admin or a student
		if(isAdmin || userName.length()==0 || password.length()==0) {
			return new Error("13|2");
		}
		if (student.containsKey(userName)){
			return new Error("13|2");
		}
		student.put(userName,password);
		//init with new implantation for ListedCourse object;
		studentData.put(userName, new ConcurrentSkipListSet<ListedCourse>(new Comparator<ListedCourse>() {
			@Override
			public int compare(ListedCourse o1, ListedCourse o2) {
				return o1.getCourseIndex().compareTo(o2.getCourseIndex());
			}
		}));
		//use index[0] to know if logged in and index[1] if admin/student
		userLog.put(userName,false);
		userType.put(userName,false);
		return new Ack("12|2");
	}

	//3
	public Command login(String userName,String password) {
		//checks if user name and password are valid and if the one who tries to register is an admin or a student
		//Checks if the userName exist and if the password is correct
		if (!(userName.length() == 0 || password.length() == 0)) {
			//check if there is already logged in user whit this user name
			if (userLog.containsKey(userName)) {
				if (userLog.get(userName)) {
					return new Error("13|3");
				}
			}
			else{
				return new Error("13|3");
			}
		}
		//if user never register we don't need to check again in admin or student tables.
		else {
			return new Error("13|3");
		}
		if (userType.get(userName)) {
			if (!(admin.containsKey(userName)) || !(admin.get(userName).equals(password))) {
				return new Error("13|3");
			}
			userLog.replace(userName,true);
			return new Ack("admin");
		}
		else{
			if (!(student.containsKey(userName)) || !(student.get(userName).equals(password))){
				return new Error("13|3");
			}
			userLog.replace(userName,true);
			return new Ack("student");
		}
	}

	//4
	public Command logout(String userName){
		userLog.replace(userName,false);
		return new Ack("12|4");
	}

	//5
	public Command courseReg(boolean isAdmin,String userName,int courseNumber){
		//Check if the course exists, if it has free spots, if the client is a student which holds
		//all kdam courses required and if not registered to the course yet
		if(isAdmin || !(courses.containsKey(courseNumber)) || !(courses.get(courseNumber).isAvailable())
				|| courses.get(courseNumber).getStudentList().contains(userName)) {
			return new Error("13|5");
		}
		int [] kdamCourse = courses.get(courseNumber).getKdamCourseList();
		for(int i: kdamCourse){
			int tempIndex = this.courses.get(i).getIndex();
			//create new ListedCourse with course courseNum and index field so we can check if student register to the curr kdamCourse
			ListedCourse tempListedCourse = new ListedCourse(i,tempIndex);
			if(!(studentData.get(userName).contains(tempListedCourse))) {
				return new Error("13|5");
			}
		}
		courses.get(courseNumber).addStudent(userName);
		Course addCourse = courses.get(courseNumber);
		studentData.get(userName).add(new ListedCourse(addCourse.getCourseNum(),addCourse.getIndex()));//change al student set tree that it wont conatain all course data but only num
		return new Ack("12|5");
	}

	//6
	public Command kdamCheck(boolean isAdmin,int courseNumber){
		//check if course exists
		if(!(courses.containsKey(courseNumber)) || isAdmin) {
			return new Error("13|6");
		}
		else{
			int [] kdamCourse = courses.get(courseNumber).getKdamCourseList();
			ConcurrentSkipListSet<ListedCourse> tempKdamCourse = new ConcurrentSkipListSet<ListedCourse>(new Comparator<ListedCourse>() {
				@Override
				public int compare(ListedCourse o1, ListedCourse o2) {
					return o1.getCourseIndex().compareTo(o2.getCourseIndex());
				}
			});
			for(int i: kdamCourse){
				int tempIndex = this.courses.get(i).getIndex();
				tempKdamCourse.add(new ListedCourse(i,tempIndex));
			}
			return new Ack("12|6|"+ tempKdamCourse);
		}
	}

	//7
	public Command courseStat(boolean isAdmin,int courseNumber){
		//check if course exists and if the client is an admin
		if(!isAdmin || !courses.containsKey(courseNumber)) {
			return new Error("13|7");
		}
		Course currCourse = courses.get(courseNumber);
		//added | after 7
		return new Ack("12|7|" + currCourse);
	}

	//8
	public Command studentStat(boolean isAdmin,String userName){
		if(!isAdmin || !(student.containsKey(userName))) {
			return new Error("13|8");
		}
		return new Ack("12|8|" + userName + "|" + studentData.get(userName));
	}

	//9
	public Command isReg(String userName,int courseNum){
		if(!courses.containsKey(courseNum)) {
			return new Error("13|9");
		}
		if(courses.get(courseNum).getStudentList().contains(userName)) {
			return new Ack("12|9|REGISTERED");
		}
		else {
			return new Ack("12|9|NOT REGISTERED");
		}
	}


	//10
	public Command unRegister(String userName, int courseNum){
		Course toRemove = this.courses.get(courseNum);
		boolean removed = toRemove.removeStudent(userName);
		if(removed) {
			ListedCourse tempToRemove = new ListedCourse(toRemove.getCourseNum(),toRemove.getIndex());
			this.studentData.get(userName).remove(tempToRemove);
			return new Ack("12|10");
		}
		else {
			return new Error("13|10");
		}
	}

	//11
	public Command myCourses(String userName){
		return new Ack("12|11|"+ studentData.get(userName));
	}
}
