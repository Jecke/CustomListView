package com.treeapps.treenotes;

import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;

public abstract class clsTreeviewIterator {
	
	private clsRepository objRepository;
	
	public clsTreeviewIterator (clsRepository objRepository) {
		this.objRepository = objRepository;
		
		
	}
	
	public void Execute() {
		int intLevel;
		for (clsTreeNode objTreeNode : objRepository.objRootNodes) {
			intLevel = 0;
			TouchTreeNodesRecursively(objTreeNode, intLevel);
		}	
	}
	
	
	private void TouchTreeNodesRecursively(clsTreeNode objTreeNode, int intLevel) {
		intLevel += 1;
		ProcessTreeNode(objTreeNode, intLevel-1);
		for(clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			TouchTreeNodesRecursively(objChildTreeNode, intLevel);
		}
		
	}


	public abstract void ProcessTreeNode(clsTreeNode objTreeNode, int intLevel);

}
