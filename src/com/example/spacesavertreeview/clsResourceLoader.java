package com.example.spacesavertreeview;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

// Helper class which creates thumbnails from images and videos.
// Note that remote vodeos (http, https) are currently not supported because
// they need to be downloaded and that can take a long time. Also a warning
// should be displayed because it would take up some download volume.
public class clsResourceLoader
{	
	private int resourceType;
	private Uri origUri;
	private TaskCompletedInterface listener;
	private Bitmap imageBitmapFull = null;
	
	private int viewWidth;
	private int viewHeight;

	// Interface for caller to get notified when the download is finished
	public interface TaskCompletedInterface 
	{
		public void loadTaskComplete(Bitmap bm, Bitmap bmFull, Uri uri);
	}
	
	// Asynchronous task to load the resource
	public class ReadRemoteResource extends AsyncTask<String, Integer, Bitmap>
	{
		private Context context;

		public ReadRemoteResource(Context context) {
			this.context = context;
		}

		// do the time consuming task of reading the file
		@Override
		protected Bitmap doInBackground (String...uri)
		{
			switch(resourceType)
			{
			case clsTreeview.IMAGE_RESOURCE:
				imageBitmapFull = retrieveRemoteImage(context, uri[0]);

				break;
				
			case clsTreeview.VIDEO_RESOURCE:
				if(!uri[0].contains("http")) 
				{
					Uri uriIn = Uri.parse(uri[0]);

					String resPath = clsUtils.getLocalPathFromUri(context, resourceType, uriIn); 

					imageBitmapFull = ThumbnailUtils.createVideoThumbnail(resPath, MediaStore.Video.Thumbnails.MICRO_KIND);
				}
				break;
			}

			return imageBitmapFull;
		}

		@Override
		protected void onPreExecute()
		{

		}

		@Override 
		protected void onPostExecute(Bitmap p)
		{
			Bitmap imageBitmap = null;

			if(p != null)
			{
				switch(resourceType)
				{
					case clsTreeview.IMAGE_RESOURCE:
						imageBitmap = ThumbnailUtils.extractThumbnail(p, clsUtils.dpToPx(50, context), 
																		 clsUtils.dpToPx(50, context));
						break;
						
					case clsTreeview.VIDEO_RESOURCE:
						// p is already the video thumbnail
						imageBitmap = p;
						break;
				}
			}	

			listener.loadTaskComplete(imageBitmap, imageBitmapFull, origUri);	
		}
	}
	
	public Bitmap retrieveRemoteImage(Context context, String uri)
	{
		InputStream is = null;
		Bitmap retval = null;

		if (uri.startsWith("http", 0))
		{
			URL[] newUrl;
			newUrl = new URL[1];
			try {
				newUrl[0] = new URL(uri);

				try {
//					is = (FileInputStream)newUrl[0].getContent();
					is = (InputStream)newUrl[0].getContent();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (uri.startsWith("content:", 0) || uri.startsWith("file:", 0))
		{
			Uri uriIn = Uri.parse(uri);

			// from http://stackoverflow.com/questions/11764392/getting-an-image-from-gallery-on-from-the-picasa-google-synced-folders-doesn
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) 
			{
				ParcelFileDescriptor parcelFileDescriptor;

				try {
					parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uriIn, "r");

					FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

					is = new FileInputStream(fileDescriptor);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
			else 
			{
				try {
					
					is = (FileInputStream)context.getContentResolver().openInputStream(uriIn);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if(is != null)
		{
			BitmapFactory.Options options = null;
			
			// If a valid maximum size is given then we calculate the sampleSize of the bitmap
			// in such a way that the image fits entirely in that size.
			if(viewWidth > 0 && viewHeight > 0)
			{			
				options = new BitmapFactory.Options();

				// First run: Get size of image without loading to memory
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, options);
				
				// Compute sampleSize of resulting bitmap
				options.inSampleSize = 
					clsUtils.calculateInSampleSize(options, viewWidth, viewHeight);

				// reset file pointer and read image for real
				options.inJustDecodeBounds = false;
				
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Re-open the input stream in order to reset to start of the stream.
				// It would also be possible to use an InputStream instead a FileInputStream and then use
				// is.getChannel().position(0) to get back to the start, but that does not support Picasa
				try {
					Uri uriIn = Uri.parse(uri);
					
					is = (FileInputStream)context.getContentResolver().openInputStream(uriIn);
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(is != null)
			{
				retval = (options == null)?BitmapFactory.decodeStream(is):BitmapFactory.decodeStream(is, null, options);
			}
		}
		
		return retval;
	}
	
	/**
	 * @param context
	 * @param resultIf			- interface to deliver result to
	 * @param resourceId		- type of resource
	 * @param uri				- URI of resource
	 * @param maxWidthFull		- if > 0 (together with maxHeightFull) then the image gets down-sampled to fit 
	 * 							  the provided dimension. This is useful to avoid out-of-memory exceptions when 
	 * 							  loading large images.
	 * @param maxHeightFull		- see above
	 */
	public void createThumbnailFromImageResource(Context context, TaskCompletedInterface resultIf, 
													int resourceId, Uri uri, 
													int maxWidthFull, int maxHeightFull)
	{
		this.resourceType = resourceId;
		this.origUri      = uri; 
		this.listener     = resultIf;
		
		viewWidth  = maxWidthFull;
		viewHeight = maxHeightFull;
		
		Bitmap imageBitmap = null;
		
		String res = uri.toString();
		Log.d("-JE-", res);

		if(resourceType == clsTreeview.VIDEO_RESOURCE && 
		   res.contains("http"))
		{
			// JE ToDo remote videos currently not supported
			clsUtils.showErrorDialog(context, R.string.video_not_supported, false);
			listener.loadTaskComplete(imageBitmap, imageBitmapFull, origUri);	

			return;
		}

		if(!res.startsWith("/", 0) || !res.startsWith("file://", 0))
		{
			// remote file
			String[] input = {res};

			// start thread to avoid blocking
			this.new ReadRemoteResource(context).execute(input);
		}
		else
		{
			String resPath = clsUtils.getLocalPathFromUri(context, resourceId, uri);

			switch(resourceType)
			{
			case clsTreeview.IMAGE_RESOURCE:
				BitmapFactory.Options options = null;
				
				// If a valid maximum size is given then we calculate the sampleSize of the bitmap
				// in such a way that the image fits entirely in that size.
				if(viewWidth > 0 && viewHeight > 0)
				{			
					options = new BitmapFactory.Options();

					// First run: Get size of image without loading to memory
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(resPath, options);
					
					// Compute sampleSize of resulting bitmap
					options.inSampleSize = 
						clsUtils.calculateInSampleSize(options, viewWidth, viewHeight);

					// reset file pointer and read image for real
					options.inJustDecodeBounds = false;
					imageBitmapFull = BitmapFactory.decodeFile(resPath, options);
				}
				else
					imageBitmapFull = BitmapFactory.decodeFile(resPath);
				
				imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmapFull, 
																clsUtils.dpToPx(50, context), 
																clsUtils.dpToPx(50, context));
				
				listener.loadTaskComplete(imageBitmap, imageBitmapFull, origUri);	
				break;
				
			case clsTreeview.VIDEO_RESOURCE:
				imageBitmap = ThumbnailUtils.createVideoThumbnail(resPath, 
						MediaStore.Video.Thumbnails.MICRO_KIND);

				listener.loadTaskComplete(imageBitmap, imageBitmapFull, origUri);	
				break;
				
			default:
				assert(false);
			}
		}
	}
}
