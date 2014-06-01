package com.treeapps.treenotes;

import java.util.ArrayList;

import android.util.Log;

public class clsTest {
	ArrayList<clsParent> objParents = new ArrayList<clsParent>();
	
	public class clsParent {
		
	}
	
	public class clsChild extends clsParent{
		String strName = "Child";
	}
	public class clsChild2 extends clsParent{
		String strName2 = "Child2";
	}
	
	public clsTest () {
		clsChild objChild = new clsChild();
		clsChild2 objChild2 = new clsChild2();
		objParents.add(objChild);
		objParents.add(objChild2);
		
		String strSerial = clsUtils.SerializeToString(objParents);
		clsUtils.CustomLog("ObjectSerial = " + strSerial);
		
		ArrayList<clsParent> objParents2 = new ArrayList<clsParent>();
		objParents2 = clsUtils.DeSerializeFromString(strSerial, objParents2.getClass());
		clsUtils.CustomLog("ObjectSerial = " + objParents2.toString());	
	}
	
	
	
}
