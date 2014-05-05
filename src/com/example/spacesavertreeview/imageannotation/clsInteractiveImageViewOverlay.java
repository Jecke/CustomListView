package com.example.spacesavertreeview.imageannotation;

import com.example.spacesavertreeview.imageannotation.clsShapeFactory.SelectedShape;
import com.example.spacesavertreeview.imageannotation.clsShapeFactory.Shape;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class clsInteractiveImageViewOverlay extends clsInteractiveImageView
											//implements OnClickListener
{
	// current values of the matrix used to pan and zoom the image as well as the overlay 
	private float overlayMatrixValues[];
    
	private clsShapeFactory.SelectedShape shapeSelected;
	private ActivityEditAnnotationImage.ShapeObserver shapeObserver;
	
	private clsShapeFactory overlay;
    
	private clsShapeFactory.SelectedShape temp;
	
    private int mode = NONE;
    
	// constructor of super class
	public clsInteractiveImageViewOverlay(Context context, AttributeSet attr)
	{
		super(context, attr);
		
		initialise();
	}
	
	public void initialise()
	{
		super.initialise();
		
		overlayMatrixValues = new float[9];
		
		overlay = new clsShapeFactory();

		shapeSelected = clsShapeFactory.SelectedShape.NONE;
		
		shapeObserver = null;
	}

	// register an observer to keep the parent activity informed about the current 
	// selection state of shapes. That is used to disable and enable menu entries.
	public void setShapeObserver(ActivityEditAnnotationImage.ShapeObserver obs)
	{
		shapeObserver = obs;
	}
	
	public float getBitmapWidth()
	{
		return bmWidth;
	}
	
	// Redraw overlay if orientation of display changes
	@Override 
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		redraw(false);
	}
	
	// 
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{	
		boolean eventConsumed = false;
		
		PointF curr = new PointF(event.getX(), event.getY());

		// This class manages an overlay over an image so we need to forward the
		// events to the image if it gets not consumed here. Events get consumed
		// if an ACTION_DOWN, _UP or MOTION_EVENTappears close to a shape.
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				float[] in_out = {last.x, last.y};
				
				// map screen coordinates to canvas coordinates
				inverseMatrix.mapPoints(in_out);

				temp = overlay.prePickShape(in_out); 
				
				if(shapeSelected == SelectedShape.NONE)
				{
					mode = NONE;
					
					// no shape selected previously and click occurred on a shape
					if(temp != SelectedShape.NONE)
					{
						mode = SHAPE_PRESELECT;
						eventConsumed = true;
					}
				}
				else
				{
					// one shape is already selected no matter if the current click occurred
					// on that shape or a different one
					mode = SHAPE_SELECT;
					
					if(temp == shapeSelected)
						eventConsumed = true;
				}
				break;
			
			// Single button up
			// - if object is pre-picked then apply pick (unselect or select or both)
			// - else -> super.onTouch
			case MotionEvent.ACTION_UP:
				
				int xDiff = (int)Math.abs(curr.x - start.x);
				int yDiff = (int)Math.abs(curr.y - start.y);
				if(xDiff < CLICK && yDiff < CLICK)
				{
					// no shape was selected before and click occurred close to a shape
					if(mode == SHAPE_PRESELECT)
					{
						shapeSelected = overlay.selectPrepickedShape();
						
						eventConsumed = true;
				
						mode = SHAPE_SELECT;

						redraw(true);
						
					}
					// A shape has been selected before. The overlay object handles the actual selection (meaning
					// whether to select or de-select a shape). The code below just changes the mode based
					// on that decision.
					else if (mode == SHAPE_SELECT)
					{
						shapeSelected = overlay.selectPrepickedShape();

						mode = (shapeSelected == SelectedShape.NONE)?(NONE):(SHAPE_SELECT);
						eventConsumed = true;
						redraw(true);
					}
					
				}
				break;
				
			// Single finger move
			// - if object pre-picked and currently selected then move or resize selected object
			// - else -> super.onTouch
			case MotionEvent.ACTION_MOVE:
				// Move is only allowed if a shape has been selected and the touch starting the movement
				// appeared near that selected shape (Note that mode is set to SHAPE_SELECT in ACTION_DOWN
				// also if the click occurred close to a different shape than the currently selected one.)
				if(mode == SHAPE_SELECT && 
				   shapeSelected == clsShapeFactory.SelectedShape.SAME &&
				   temp == shapeSelected)
				{
					float[] c = {curr.x, curr.y};
					float[] l = {last.x, last.y};
					
					// map screen coordinates to canvas coordinates
					inverseMatrix.mapPoints(c);
					inverseMatrix.mapPoints(l);
					
					float[] delta = {c[0] - l[0], 
									 c[1] - l[1]};
					
					overlay.applyOffsetToSelected(delta[0], delta[1]);
					redraw(false);
					
					eventConsumed = true;

					last.set(curr.x, curr.y);
				}
				break;
				
			// POINTER_UP, POINTER_DOWN and everything else
			// - super.onTouch
			default:
				break;
		}

		// Events not consumed in this method will be handled by super class
		if(!eventConsumed)
		{
			return super.onTouch(v, event);
		}

		return true;
	}

	// Needs to be called after the image has been loaded.
	// Performs initializations which require the ImageView to be valid
	public void startMatrixOperation()
	{		
		super.startMatrixOperation();
		
		redraw(false);
	}

	public void changeAttributesOfSelectedShape(clsAnnotationData.AttributeContainer attr)
	{
		if(overlay.changeAttributesOfSelectedShape(attr))
		{
			redraw(true);
		}
	}

	public void getAttributesOfSelectedShape(clsAnnotationData.AttributeContainer attr)
	{
		attr = overlay.getAttributesOfSelectedShape();
	}
	
	public String getDescriptionOfSelectedObject()
	{
		return overlay.getDescriptionOfSelectedObject();
	}
	
	public void changeDescriptionOfSelectedShape(String description)
	{
		overlay.changeDescriptionOfSelectedShape(description);
	}
	
	public void addRectangleOverlay(clsAnnotationData.AttributeContainer attr)
	{
		// for now the reference point (center of the rectangle) is initial in the 
		// middle of the visible area of the screen
//		float[] coord = {width/2 - RECTANGLE_OFFSET_HOR, height/2 - RECTANGLE_OFFSET_VER,
//						 width/2 + RECTANGLE_OFFSET_HOR, height/2 + RECTANGLE_OFFSET_VER};
//		inverseMatrix.mapPoints(coord);
//		
		float offset = Math.min(bmWidth, bmHeight)/3;
		float[] coord = {width/2, height/2, 0f, 0f};
		
		inverseMatrix.mapPoints(coord);
		
		coord[2] = coord[0] + offset;
		coord[3] = coord[1] + offset;

		if(overlay.createShape(context,
								Shape.RECTANGLE, 
								"",
								attr,
								coord,
								bmWidth, bmHeight,
								Math.min(width, height),
								true))
		{
			mode = SHAPE_SELECT;
			shapeSelected = SelectedShape.SAME;
			
			// redraw
			redraw(true);
		}
	}
	
	public void addArrowOverlay(clsAnnotationData.AttributeContainer attr)
	{
		// for now the reference point (arrow tip) is initial in the 
		// middle of the visible area of the screen
		//float minDim = Math.min(bmWidth, bmHeight);

		// Compute default reference points. The length of the
		// arrow is dependent of the bitmap size.
		// The first point is the middle of the screen. It must be mapped 
		// to the bitmap. The second point is on the bitmap.
		float offset = Math.min(bmWidth, bmHeight)/3;
		float[] coord = {width/2, height/2, 0f, 0f};
		
		inverseMatrix.mapPoints(coord);
		
		coord[2] = coord[0];
		coord[3] = coord[1] + offset;
		
		if(overlay.createShape(context,
								Shape.ARROW, 
								"",
								attr,
								coord,
								bmWidth, bmHeight,
								Math.min(width, height),
								true))
		{
			mode = SHAPE_SELECT;
			shapeSelected = SelectedShape.SAME;
			
			// redraw
			redraw(true);
		}
	}

	public void addNumberedArrowOverlay(clsAnnotationData.AttributeContainer attr)
	{
		// for now the reference point (arrow tip) is initial in the 
		// middle of the visible area of the screen
		float offset = Math.min(bmWidth, bmHeight)/4;
		float[] coord = {width/2, height/2, 0f, 0f};

		inverseMatrix.mapPoints(coord);
		
		coord[1] = coord[1] - offset;
		coord[2] = coord[0];
		coord[3] = coord[1] + offset * 2;
		
		if(overlay.createShape(context,
								Shape.NUMBERED_ARROW, 
								"",
								attr,
								coord,
								bmWidth, bmHeight,
								Math.min(width, height),
								true))
		{
			mode = SHAPE_SELECT;
			shapeSelected = SelectedShape.SAME;
			
			// redraw
			redraw(true);
		}
	}

	// remove the currently selected shape from the overlay
	public void deleteSelectedShape()
	{
		if(overlay.deleteSelectedShape())
		{
			redraw(true);
		}
	}
	
	// Fills the provided clsAnnotation object with the graphical shapes and texts which
	// are currently defined.
	public void extractAnnotations(clsAnnotationData data)
	{
		overlay.extractAnnotations(data);
	}
	
	// Create shapes defined in data
	// The points are already mapped so no matrix operation is necessary
	public void createShapes(clsAnnotationData data)
	{
		for(clsAnnotationData.clsAnnotationItem item : data.items)
		{
			float[] ptr;
			
			switch(item.getType())
			{
				case RECTANGLE:
				{
					float[] coord = {item.getReferencePoints().get(0).x,
									 item.getReferencePoints().get(0).y,
									 item.getReferencePoints().get(1).x,
									 item.getReferencePoints().get(1).y};
				
					ptr = coord;
				}
				break;
				
				case ARROW:
				{
					float[] coord = {item.getReferencePoints().get(0).x,
									 item.getReferencePoints().get(0).y,
									 item.getReferencePoints().get(1).x,
									 item.getReferencePoints().get(1).y};
					
					ptr = coord;
				}
				break;
				
				case NUMBERED_ARROW:
				{
					float[] coord = {item.getReferencePoints().get(0).x,
									 item.getReferencePoints().get(0).y,
									 item.getReferencePoints().get(1).x,
									 item.getReferencePoints().get(1).y};
					
					ptr = coord;
				}
				break;
				
				// that should never happen; skip to next iteration
				default:
					continue;
			}

			// Note: Method can be simplified further because all shapes have the same
			// number of reference points, but the real common part is the creation
			// of the actual shapes.
			//inverseMatrix.mapPoints(ptr);
			
			overlay.createShape(context, 
								item.getType(), 
								item.getAnnotationText(),
								item.getAttributes(),
								ptr,
								bmWidth, 
								bmHeight,
								Math.min(width, height),
								false);
		}
		
		if(!data.items.isEmpty())
		{
			// redraw without a selected object
			redraw(false);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		drawPending = false;

		canvas.drawColor(Color.TRANSPARENT);

		// Todo: obviously the matrix for zooming and panning is applied differently
		// on the image than on the canvas. Here we have to swap the transformation
		// to translate first and scale second. That is unlucky and we should 
		// try to fix that in the super class clsInteractiveImageView.
		// If it gets fixed then a simple canvas.setMatrix(matrix) should be sufficient 
		// to handle the overlay graphics. overlayMatrixValues as well
		// as canvas.translate and canvas.scale can be removed then. 
		matrix.getValues(overlayMatrixValues);

		canvas.save();

		canvas.translate(overlayMatrixValues[Matrix.MTRANS_X], 
						 overlayMatrixValues[Matrix.MTRANS_Y]);
		canvas.scale(overlayMatrixValues[Matrix.MSCALE_X], 
					 overlayMatrixValues[Matrix.MSCALE_Y]);

		overlay.drawAll(canvas);
		
		canvas.restore();
	}

	// Helper to redraw the graphics. It also notifies the observer if required.
	private void redraw(boolean notifyShapeObserver)
	{
		if(notifyShapeObserver && shapeObserver != null)
		{
			shapeObserver.notify(overlay.getSelectedShapeType());
		}
		invalidate();
	}
}