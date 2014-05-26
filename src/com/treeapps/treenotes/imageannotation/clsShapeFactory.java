package com.treeapps.treenotes.imageannotation;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;

/**
 * @author Ultraventris
 *
 * Factory producing shapes for the image overlay
 */
public class clsShapeFactory {

	// A shape is considered to be selected (picked) if the touch events 
	// occurs at most MIN_PICK_DISTANCE pixels from a vertex of that shape.
	private static int MIN_PICK_DISTANCE = 30;

	public enum Shape
	{
		RECTANGLE,
		ARROW,
		NUMBERED_ARROW,
		
		NONE
	};

	public enum SelectedShape
	{
		SAME,
		DIFFERENT,
		NONE
	};
	
	// list of overlay shapes
	private ArrayList<clsShapeRectangle> mRectangleList;
	private ArrayList<clsShapeArrow> mArrowList;
	private ArrayList<clsShapeNumberedArrow> mNumberedArrowList;

	// keep reference to selected object
	clsShapeBase objSelectedShape;
	clsShapeBase objPreSelectedShape;

	/*
	 // angle between two vectors 
	 // Center point is p1; angle returned in Radians
	function findAngle(p0,p1,p2) {
    var b = Math.pow(p1.x-p0.x,2) + Math.pow(p1.y-p0.y,2),
        a = Math.pow(p1.x-p2.x,2) + Math.pow(p1.y-p2.y,2),
        c = Math.pow(p2.x-p0.x,2) + Math.pow(p2.y-p0.y,2);
    return Math.acos( (a+b-c) / Math.sqrt(4*a*b) );
}
	 */

	/*
	 * Constructor
	 * 
	 * Do initialisation
	 */
	public clsShapeFactory()
	{
		// allocate list of shapes
		mRectangleList 		= new ArrayList<clsShapeRectangle>();
		mArrowList 			= new ArrayList<clsShapeArrow>();
		mNumberedArrowList 	= new ArrayList<clsShapeNumberedArrow>();
		
		// no object selected at the beginning
		objSelectedShape    = null;
		objPreSelectedShape = null;
	}
	
	// create specified default shape and add it to internal list
	/*
	 * in: type  - enumeration
	 * in: color
	 * in: lineWidth in px
	 * in: ls - line style enumeration
	 * reference - 	reference point of shape
	 * 				rectangle - midpoint
	 * 				arrow     - top of arrow head
	 * maxWidth  - defines right limit of drawing area (bitmap)
	 * maxHeight - defines bottom limit of drawing area (bitmap)
	 */
	public boolean createShape(Context context, Shape type, String text,
								clsAnnotationData.AttributeContainer attributes,
								float[] reference, float maxWidth, float maxHeight, 
								float minScreenDim,
								boolean select)
	{
		// reset selection because new object will get selected automatically
		for(clsShapeRectangle item : mRectangleList)
		{
			item.setSelected(false);
		}
		for(clsShapeArrow item : mArrowList)
		{
			item.setSelected(false);
		}
		for(clsShapeNumberedArrow item : mNumberedArrowList)
		{
			item.setSelected(false);
		}
		
		// create shape
		switch(type)
		{
			case RECTANGLE:
			{
				clsShapeRectangle obj = 
					new clsShapeRectangle(context, attributes, reference,
										  maxWidth, maxHeight, minScreenDim);
				obj.setSelected(select);
				
				mRectangleList.add(obj);
				
				objSelectedShape = obj;
			}
			break;
				
			case ARROW:
			{
				clsShapeArrow obj = new clsShapeArrow(context, attributes, reference,
														maxWidth, maxHeight, minScreenDim);
				obj.setSelected(select);
				
				mArrowList.add(obj);
				
				objSelectedShape = obj;
			}
			break;
				
			case NUMBERED_ARROW:
			{
				clsShapeNumberedArrow obj = 
					new clsShapeNumberedArrow(context, attributes, text, reference, 
												maxWidth, maxHeight, minScreenDim, 
												mNumberedArrowList.size() + 1);
				obj.setSelected(select);
				
				mNumberedArrowList.add(obj);
				
				objSelectedShape = obj;
			}
			break;
			
			default:
				assert false;
				return false;
		}
		
		return true;
	}
	
	public boolean changeAttributesOfSelectedShape(clsAnnotationData.AttributeContainer attributes)
	{
		if(objSelectedShape != null)
		{
			objSelectedShape.changeAttributes(attributes); 

			return true;
		}
		else
		{
			return false;
		}
	}
	
	public clsAnnotationData.AttributeContainer getAttributesOfSelectedShape()
	{
		if(objSelectedShape != null)
		{
			return(objSelectedShape.getAttributes()); 
		}
		return null;
	}
	
	public String getDescriptionOfSelectedObject()
	{
		String retval = "";
		
		if(objSelectedShape != null && 
		   objSelectedShape.getSelectedShapeType() == Shape.NUMBERED_ARROW)
		{
			clsShapeNumberedArrow temp = mNumberedArrowList.get(
					mNumberedArrowList.indexOf(objSelectedShape));
			
			retval = temp.getDescription();
		}
			
		return(retval);
	}
	
	public void changeDescriptionOfSelectedShape(String description)
	{
		if(objSelectedShape != null && 
		   objSelectedShape.getSelectedShapeType() == Shape.NUMBERED_ARROW)
		{
			clsShapeNumberedArrow temp = mNumberedArrowList.get(
				mNumberedArrowList.indexOf(objSelectedShape));
					
			temp.setDescription(description);
		}
	}

	public boolean deleteSelectedShape()
	{
		if(objSelectedShape != null)
		{
			switch(objSelectedShape.getSelectedShapeType())
			{
			case RECTANGLE:
				if(mRectangleList.contains(objSelectedShape))
				{
					mRectangleList.remove(objSelectedShape);
					
					objSelectedShape = null;

					return true;
				}

				break;
				
			case ARROW:
				if(mArrowList.contains(objSelectedShape))
				{
					mArrowList.remove(objSelectedShape);
					
					objSelectedShape = null;

					return true;
				}
				break;
				
			case NUMBERED_ARROW:
				if(mNumberedArrowList.contains(objSelectedShape))
				{
					mNumberedArrowList.remove(objSelectedShape);
					
					objSelectedShape = null;
					
					// renumber arrows
					for(int i = 0; i < mNumberedArrowList.size(); i++)
					{
						mNumberedArrowList.get(i).setNumber(i + 1);
					}

					return true;
				}
				break;
				
			default:
				break;
			}
		}
		
		return false;
	}
	
	// Fills the provided clsAnnotation object with the graphical shapes and texts which
	// are currently defined.
	public void extractAnnotations(clsAnnotationData data)
	{
		clsAnnotationData.clsAnnotationItem elem;
		
		for(clsShapeRectangle item : mRectangleList)
		{
			elem = data.new clsAnnotationItem();
			
			item.extractAnnotation(elem);
			
			// That is the standard ID no matter whether the shape has a text assigned
        	elem.setResourceId(clsAnnotationData.EDIT_ANNOTATION_TEXT);
        	
        	data.addAnnotationElement(elem);
		}
		 
		for(clsShapeArrow item : mArrowList)
		{
			elem = data.new clsAnnotationItem();
			
			item.extractAnnotation(elem);

			// That is the standard ID no matter whether the shape has a text assigned
        	elem.setResourceId(clsAnnotationData.EDIT_ANNOTATION_TEXT);
        	
        	data.addAnnotationElement(elem);
		}
		 
		for(clsShapeNumberedArrow item : mNumberedArrowList)
		{
			elem = data.new clsAnnotationItem();
			
			item.extractAnnotation(elem);
			
        	elem.setResourceId(clsAnnotationData.EDIT_ANNOTATION_TEXT);
        	
        	data.addAnnotationElement(elem);
		}
	}
	
	// Checks if a shape is close to the provided canvas coordinates without applying
	// the selection yet. The reason is that it needs an ACTION_UP from the user to actually 
	// pick a shape but the ACTION_DOWN event initiates the picking. The following action could
	// either be an ACTION_UP or ACTION_MOVE
	public SelectedShape prePickShape(float[] coord)
	{
		boolean selected = false;
		
		SelectedShape retval = SelectedShape.NONE;
		
		objPreSelectedShape = null;
		
		// First check current selected shape.
		// That is to handle overlapping shapes. It makes sure that the currently selected shape
		// can be moved if it is partly obscured by another shape and the click occurs in that position.
		if(objSelectedShape != null)
		{
			clsShapeFactory.Shape type = objSelectedShape.getSelectedShapeType();
			
			switch(type)
			{
				case ARROW:
				{
					clsShapeArrow item = (clsShapeArrow)objSelectedShape; 
					
					if(item.distanceToPick(coord) <= MIN_PICK_DISTANCE)
					{
						objPreSelectedShape = item;
						
						selected = true;
					}
				}
				break;
				
				case NUMBERED_ARROW:
				{
					clsShapeNumberedArrow item = (clsShapeNumberedArrow)objSelectedShape; 
					
					if(item.distanceToPick(coord) <= MIN_PICK_DISTANCE)
					{
						objPreSelectedShape = item;
						
						selected = true;
					}
				}
				break;
				
				case RECTANGLE:
				{
					clsShapeRectangle item = (clsShapeRectangle)objSelectedShape; 
					
					if(item.distanceToPick(coord) <= MIN_PICK_DISTANCE)
					{
						objPreSelectedShape = item;
						
						selected = true;
					}
				}
				break;
				
				default:
					break;
			}
		}
		
		if(!selected)
		{
			// check rectangles
			for(clsShapeRectangle item : mRectangleList)
			{
				if(item.distanceToPick(coord) <= MIN_PICK_DISTANCE)
				{
					objPreSelectedShape = item;

					selected = true;

					break;
				}
			}

			// check arrows
			for(clsShapeArrow item : mArrowList)
			{
				if(item.distanceToPick(coord) <= MIN_PICK_DISTANCE)
				{
					objPreSelectedShape = item;

					selected = true;

					break;
				}
			}

			// check numbered arrows
			for(clsShapeNumberedArrow item : mNumberedArrowList)
			{
				if(item.distanceToPick(coord) <= MIN_PICK_DISTANCE)
				{
					objPreSelectedShape = item;

					selected = true;

					break;
				}
			}
		}
		if(selected)
		{
			if(objPreSelectedShape == objSelectedShape)
			{
				retval = SelectedShape.SAME;
			}
			else
			{
				retval = SelectedShape.DIFFERENT;
			}
		}
		
		return retval;
	}

	// change or toggle selection of an overlay shape
	public SelectedShape selectPrepickedShape()
	{
		SelectedShape retval = SelectedShape.NONE;
		
		if(objSelectedShape != null)
		{
			// Case 1: The same shape has been selected again -> toggle
			if(objPreSelectedShape == objSelectedShape)
			{
				objSelectedShape.setSelected(!objSelectedShape.selected);
				
				if(!objSelectedShape.selected)
				{
					objSelectedShape = null;
					
					retval = SelectedShape.NONE;
				}
				else
					retval = SelectedShape.SAME;
				
				objPreSelectedShape = null;
			}
			// Case 2: A different shape has been selected
			// -> de-select current shape and select new shape if not null
			else
			{
				objSelectedShape.setSelected(false);

				if(objPreSelectedShape != null)
				{
					objPreSelectedShape.setSelected(true);
					objSelectedShape = objPreSelectedShape;
					retval = SelectedShape.SAME;
				}
				else
				{
					objSelectedShape = null;
					retval = SelectedShape.NONE;
				}
			}
		}
		// no shape is currently selected
		else
		{
			// Case 3: The pre-picked shape becomes the new selected shape
			if(objPreSelectedShape != null)
			{
				objSelectedShape = objPreSelectedShape;
				objSelectedShape.setSelected(true);
				
				objPreSelectedShape = null;
				
				retval = SelectedShape.SAME;
			}
			else
				retval = SelectedShape.NONE;
		}

		return retval;
	}

	public void applyOffsetToSelected(float deltaX, float deltaY)
	{
		if(objSelectedShape != null)
		{
			objSelectedShape.applyOffset(deltaX, deltaY);
		}
	}

	// draw all shapes 
	// treat selected shape differently 
	public void drawAll(Canvas canvas)
	{
		// draw rectangles
		for(clsShapeRectangle item : mRectangleList)
		{
			item.draw(canvas);
		}
		
		// draw arrows
		for(clsShapeArrow item : mArrowList)
		{
			item.draw(canvas);
		}

		// draw numbered arrows
		for(clsShapeNumberedArrow item : mNumberedArrowList)
		{
			item.draw(canvas);
		}
	}
	
	public Shape getSelectedShapeType()
	{
		if(objSelectedShape == null)
		{
			return Shape.NONE;
		}
		else
		{
			return objSelectedShape.getSelectedShapeType();
		}
	}
}
