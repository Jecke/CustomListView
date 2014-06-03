
package com.treeapps.treenotes.imageannotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;

import com.treeapps.treenotes.clsUtils;

/**
 * @author 
 *
 * This class combines a file and the associated annotation data into a new
 * file and saves it.
 * It takes into account the maximum dimensions provided and takes care that
 * the image is completely visible by computing the appropriate sample size.
 */
public class clsCombineAnnotateImage {

	private Context objContext;
	private String strAnnotatedFile;
	private ProgressDialog pd;
	private TaskCompletedInterface _listener;
	
	private int maxWidth;
	private int maxHeight;
	
	// Interface for caller to get notified when the download is finished
	public interface TaskCompletedInterface 
	{
		public void loadTaskComplete(String combinedFile);
	}
	
	public clsCombineAnnotateImage(Context context, TaskCompletedInterface listener)
	{
		objContext = context;
		_listener = listener;
	}

	public void createAnnotatedImage(String original, String annotated, clsAnnotationData annotationData, int maxWidth, int maxHeight)
	{
		strAnnotatedFile = original;
		
		this.maxWidth  = maxWidth;
		this.maxHeight = maxHeight;
		
		if(annotationData != null)
		{
			// handle annotationData with default values
			if(annotationData.compressionRate == 0 && annotationData.items.isEmpty())
			{
				_listener.loadTaskComplete(strAnnotatedFile);
				return;
			}
			
			// 1. Create the name of the new file
			strAnnotatedFile = annotated;

			pd = ProgressDialog.show(objContext, "Processing...", "Please wait", 
					true, true, null);

			loadImage(strAnnotatedFile, annotationData);
		}
		else
			_listener.loadTaskComplete(strAnnotatedFile);
	}

	private void loadImage(String outFile, clsAnnotationData data)
	{
		String[] input = {outFile};
		
		new LoadImage(data).execute(input);
	}

	// ============================= AsyncTask =============================
	// Async task to load images
	public class LoadImage extends AsyncTask<String, Integer, String>
	{
		int quality = 100;
		String strInFile;
		clsAnnotationData objData;
		
		public LoadImage(clsAnnotationData data) 
		{
			objData = data;
			
			strInFile = objData.strLocalImage;
			
			quality = 100 - objData.compressionRate;
		}
		
		protected String doInBackground(String...outFile)
		{
			String out = outFile[0];
			Bitmap imageBitmap = null;

			OutputStream fOutputStream = null;

			// Create bitmap from original file. The new bitmap must be mutable to draw on it.
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inMutable = true;

			imageBitmap = clsUtils.downsampleImageToView(strInFile, maxWidth, maxHeight, true);
			
			// write back compressed file if necessary
			if(quality < 100)
			{
				try {
					fOutputStream = new FileOutputStream(new File(out));

					imageBitmap.compress(CompressFormat.JPEG, quality, fOutputStream);

					fOutputStream.flush();
					fOutputStream.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return "";
				} catch (IOException e) {
					e.printStackTrace();
					return "";
				}
				
				// read the compressed image
				imageBitmap = BitmapFactory.decodeFile(out, opts);
			}
			
			if(!objData.items.isEmpty())
			{
				int numberedArrowId = 1;
				
				// loop through shapes and draw them on top of the bitmap
				Canvas canvas = new Canvas(imageBitmap);
				
				int minScreenDim = Math.min(maxWidth, maxHeight);
				
				for(clsAnnotationData.clsAnnotationItem item: objData.items)
				{
					// simplified version because all supported shapes have 
					// two reference points
					float[] coord = {item.getReferencePoints().get(0).x,
							 item.getReferencePoints().get(0).y,
							 item.getReferencePoints().get(1).x,
							 item.getReferencePoints().get(1).y};

					switch(item.getType())
					{
						case RECTANGLE:
						{
							clsShapeRectangle shape = 
							new clsShapeRectangle(objContext, item.getAttributes(), coord, 
									  imageBitmap.getWidth(), imageBitmap.getHeight(),
									  minScreenDim);
							
							shape.draw(canvas);
						}
						break;
						
						case ARROW:
						{
							clsShapeArrow shape = 
								new clsShapeArrow(objContext, item.getAttributes(), coord, 
												  imageBitmap.getWidth(), imageBitmap.getHeight(),
												  minScreenDim);
							
							shape.draw(canvas);
						}
						break;
						
						case NUMBERED_ARROW:
						{
							clsShapeNumberedArrow shape = 
							new clsShapeNumberedArrow(objContext, item.getAttributes(), item.getAnnotationText(), coord, 
											  			imageBitmap.getWidth(), imageBitmap.getHeight(), 
											  			minScreenDim, numberedArrowId);
							numberedArrowId++;
						
							shape.draw(canvas);
						}
						break;
						
						default:
							break;
					}
				}
				
				// write annotated bitmap back to file uncompressed
				try {
					fOutputStream = new FileOutputStream(new File(out));

					imageBitmap.compress(CompressFormat.JPEG, 100, fOutputStream);

					fOutputStream.flush();
					fOutputStream.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return "";
				} catch (IOException e) {
					e.printStackTrace();
					return "";
				}
			}
			
			return out;
		}
		
//		@Override
//		protected void onPreExecute()
//		{
//
//		}

		@Override 
		protected void onPostExecute(String outFile)
		{
			if(!outFile.isEmpty())
			{
				_listener.loadTaskComplete(outFile);
			}
			
			pd.dismiss();
		}
	}

}
