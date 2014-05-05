package com.example.spacesavertreeview.imageannotation;

import java.util.ArrayList;

import com.example.spacesavertreeview.clsUtils;

import android.graphics.PointF;

// The interface class which is used to exchange data between the main activity and the image annotation classes.
public class clsAnnotationData {

	public static final String DESCRIPTION   = "com.example.spacesavertreeview.imageannotation.description";
	public static final String RESOURCE_PATH = "com.example.spacesavertreeview.imageannotation.resource_path";
	public static final String TREENODE_UID  = "com.example.spacesavertreeview.imageannotation.treenode_uuid";
	
	public static final String DATA          = "com.example.spacesavertreeview.imageannotation.data";
	public static final String CHANGED       = "com.example.spacesavertreeview.imageannotation.changed";

	public static final int INVALID = -1;
	public static final int EDIT_ANNOTATION_TEXT = 0;
	public static final int EDIT_ANNOTATION_IMAGE = 1;
	
	public class AttributeContainer
	{
		public int lineWidth;
		public clsUtils.Linestyle lineStyle;
		public int lineColor;
		public int textColor;
		
		AttributeContainer(int lineWidth, clsUtils.Linestyle lineStyle, int lineColor, int textColor)
		{
			this.lineWidth = lineWidth;
			this.lineStyle = lineStyle;
			this.lineColor = lineColor;
			this.textColor = textColor;
		}
		AttributeContainer(AttributeContainer attr)
		{
			this(attr.lineWidth, attr.lineStyle, attr.lineColor, attr.textColor);
		}

		public void setAttributes(AttributeContainer attr)
		{
			this.lineWidth = attr.lineWidth;
			this.lineStyle = attr.lineStyle;
			this.lineColor = attr.lineColor;
			this.textColor = attr.textColor;
		}
	}
	
	// each object of that class defines an annotation element 
	public class clsAnnotationItem
	{
		private int  resourceId;

		private String annotationText = "";				// for now only used for numbered arrows
		
		private String resourcePath = "";				// only used for images
		private String treeNodeGuid = "";				// only used for images
		
		private clsShapeFactory.Shape type;			// defines meaning of reference points
		private ArrayList<PointF> referencePoints;	// reference points
		
		// attributes
		private AttributeContainer attribute;
		
		// Todo: may need more information like radius of corners of rectangle, size and length of arrow head,
		// radius of circle enclosing numbers on arrows.
		
		public clsAnnotationItem() 
		{
			referencePoints = new ArrayList<PointF>();
		}

		public clsAnnotationItem(clsAnnotationItem item) {
			
			resourceId = item.resourceId;
			resourcePath = item.resourcePath;
			treeNodeGuid = item.treeNodeGuid;
			
			annotationText = item.annotationText;
			type = item.type;

			referencePoints = new ArrayList<PointF>(item.getReferencePoints().size());
			for(PointF p : item.getReferencePoints())
			{
				referencePoints.add(new PointF(p.x, p.y));
			}
			
			attribute = item.attribute;
		}

		public int getResourceId() {
			return resourceId;
		}

		public void setResourceId(int resourceId) {
			this.resourceId = resourceId;
		}

		public String getResourcePath() {
			return resourcePath;
		}

		public void setResourcePath(String resourcePath) {
			this.resourcePath = resourcePath;
		}

		public String getTreeNodeGuid() {
			return treeNodeGuid;
		}

		public void setTreeNodeGuid(String treeNodeGuid) {
			this.treeNodeGuid = treeNodeGuid;
		}

		public String getAnnotationText() {
			return annotationText;
		}

		public void setAnnotationText(String annotationText) {
			this.annotationText = annotationText;
		}

		public clsShapeFactory.Shape getType() {
			return type;
		}

		public void setType(clsShapeFactory.Shape type) {
			this.type = type;
		}

		public ArrayList<PointF> getReferencePoints() {
			return referencePoints;
		}

		public void addReferencePoint(float x, float y)
		{
			referencePoints.add(new PointF(x, y));
		}
		
		public void setReferencePoints(ArrayList<PointF> referencePoints) {
			this.referencePoints = referencePoints;
		}

		public AttributeContainer getAttributes()
		{
			return attribute;
		}

		public void setAttributes(AttributeContainer attr)
		{
			attribute = attr;
		}
	}
	// end: class clsAnnotationItem
	
	// Identifier of image to annotate
	public String strNodeUuid;
	public String strDescription;
	public String strResourcePath;
	public String strLocalImage;
	
	public int resourceId;
	public int compressionRate;
	
	// The scaling factor of the original image to the image which
	// is actually displayed on the screen. It is used to up-scale 
	// the overlay shapes to the size of the original image.
	public float[] sampleSize = new float[2];  
	
	public ArrayList<clsAnnotationItem> items;
	
	// constructor
	// set UUID of image and allocate space for annotation
	public clsAnnotationData(String parentNodeUuid) 
	{
		// Save identifier of root 
		strNodeUuid = parentNodeUuid;
		
		items = new ArrayList<clsAnnotationItem>();
	}
	
	// Add the description of the annotation element (geometry + Metadata)
	public void addAnnotationElement(clsAnnotationItem item)
	{
		// create a copy of the item 
		items.add(new clsAnnotationItem(item));
	}

	public void clear()
	{
		items.clear();
	}
}
