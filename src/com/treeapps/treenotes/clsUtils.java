package com.treeapps.treenotes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.CRC32;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.treeapps.treenotes.ActivityExplorerStartup.clsIabLocalData;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData.clsImageToBeUploadedData;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptionSearch.clsListViewStateSearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class clsUtils {

	public enum Linestyle {
		LS_SOLID, LS_DOTTED_5, LS_DOTTED_10, LS_DASHED_10_5, LS_DASHED_10_5_3_5,

		// linestyle to draw user-selected shapes
		LS_RUBBERBAND
	}

	static String strDateTimeFormat = "dd MM yyyy hh:mm:ss Z";

	public static void MessageBox(Context context, String strMessage, boolean boolDisplayAsToast) {

		if (!boolDisplayAsToast) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(strMessage);
			builder.setCancelable(true);
			builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
		}
	}

	public static String RemoveExtension(String in) {
		int p = in.lastIndexOf(".");
		if (p < 0)
			return in;

		int d = in.lastIndexOf(File.separator);

		if (d < 0 && p == 0)
			return in;

		if (d >= 0 && d > p)
			return in;

		return in.substring(0, p);
	}

	public static void CopyFileAlt(File source, File dest) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;

		try {

			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	// The method copies files by using Streams. Another possibility would be to
	// use
	// Channels (see above) but the Internet says that this will fail with VERY
	// large
	// files without actually specifying how large 'VERY large' is.
	public static void CopyFile(File src, File dst, boolean aAppend) {
		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			try {
				byte[] bucket = new byte[32 * 1024];

				inStream = new BufferedInputStream(new FileInputStream(src));
				outStream = new BufferedOutputStream(new FileOutputStream(dst, aAppend));

				int bytesRead = 0;
				while (bytesRead != -1) {
					bytesRead = inStream.read(bucket); // -1, 0, or more

					if (bytesRead > 0) {
						outStream.write(bucket, 0, bytesRead);
					}
				}
			} finally {
				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
			}
		} catch (FileNotFoundException ex) {
			clsUtils.CustomLog("File not found: " + ex);
		} catch (IOException ex) {
			clsUtils.CustomLog(ex.getMessage());
		}
	}

	public static File BuildExplorerFilename(File filePath, String strFilename) {
		File objChildFile = new File(filePath, strFilename + ".treenotes");
		return objChildFile;
	}

	public static File BuildNoteFilename(File filePath, String strFilename) {
		File objChildFile = new File(filePath, strFilename + ".treenote");
		return objChildFile;
	}

	public static File BuildUsersRepositoryFilename(File filePath) {
		File objChildFile = new File(filePath, "groupmembers.users");
		return objChildFile;
	}

	public static File BuildTempNoteFilename(File filePath) {
		File objChildFile = new File(filePath, "temp.treenote");
		return objChildFile;
	}

	public static String GetTreeNotesDirectoryName(Context context) {
		String root = Environment.getExternalStorageDirectory().toString();
		SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String strTreeNodesFolderName = mySharedPreferences.getString("treenotes_root_folder_name", "");
		if (strTreeNodesFolderName.length() == 0) {
			strTreeNodesFolderName = context.getResources().getString(R.string.treenodes_dirname);
		}
		return root + "/" + strTreeNodesFolderName;
	}

	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static int pxToDp(Context context, int px) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return dp;
	}

	public static int GetDisplayWidthInDp(Activity objActivity) {
		DisplayMetrics dm = new DisplayMetrics();
		objActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return pxToDp(objActivity, dm.widthPixels);
	}

	public static int GetDisplayHeightInDp(Activity objActivity) {
		DisplayMetrics dm = new DisplayMetrics();
		objActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return pxToDp(objActivity, dm.heightPixels);
	}

	public static int GetDisplayMaxWidthInDp(Activity objActivity) {
		DisplayMetrics dm = new DisplayMetrics();
		objActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return pxToDp(objActivity, Math.max(dm.heightPixels, dm.widthPixels));
	}

	public static int GetDisplayMinWidthInDp(Activity objActivity) {
		DisplayMetrics dm = new DisplayMetrics();
		objActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return pxToDp(objActivity, Math.min(dm.heightPixels, dm.widthPixels));
	}

	public static void showErrorDialog(Context context, int textId, boolean cancelable) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(textId);
		builder.setCancelable(cancelable);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// helper: retrieve the path of a resource from the internal database
	// by using the URI
	public static String getLocalPathFromUri(Context context, int resourceId, Uri uri) {
		String path = "";
		String[] filePathColumn;
		Cursor cursor;
		int columnIndex;

		String temp = uri.toString();
		if (temp.startsWith("/")) {
			return temp;
		}

		switch (resourceId) {
		case clsTreeview.IMAGE_RESOURCE:
		case clsTreeview.WEB_RESOURCE: {
			filePathColumn = new String[2];
			filePathColumn[0] = MediaStore.Images.Media.DATA;
			filePathColumn[1] = MediaStore.Images.Media.DISPLAY_NAME;
		}
			break;

		case clsTreeview.VIDEO_RESOURCE: {
			filePathColumn = new String[2];
			filePathColumn[0] = MediaStore.Video.Media.DATA;
			filePathColumn[1] = MediaStore.Video.Media.DISPLAY_NAME;
		}
			break;

		default:
			assert (false);
			return path;
		}

		// retrieve the file path from the database
		try {
			cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
		} catch (Exception e) {
			return path;
		}
		if (cursor != null) {
			cursor.moveToFirst();

			columnIndex = cursor.getColumnIndex(filePathColumn[0]);

			path = cursor.getString(columnIndex);
			columnIndex = cursor.getColumnIndex(filePathColumn[1]);

			cursor.close();
		}

		return path;
	}

	public static PathEffect getPathEffect(Linestyle ls) {
		PathEffect pe = null;

		switch (ls) {
		case LS_SOLID:
			// path effect is null for solid lines
			break;

		case LS_DASHED_10_5:
			pe = new DashPathEffect(new float[] { 10, 5 }, 0);
			break;

		case LS_DASHED_10_5_3_5:
			pe = new DashPathEffect(new float[] { 10, 5, 3, 5 }, 0);
			break;

		case LS_DOTTED_10:
			pe = new DashPathEffect(new float[] { 10, 10 }, 0);
			break;

		case LS_DOTTED_5:
			pe = new DashPathEffect(new float[] { 5, 5 }, 0);
			break;

		// linestyle to draw user-selected shapes
		case LS_RUBBERBAND:
			pe = new DashPathEffect(new float[] { 5, 5 }, 0);
			break;
		}

		return pe;
	}

	// computes the distance in pixels between a reference and a set of points
	public static double[] distanceBetweenPoints(PointF[] a, PointF b) {
		double[] retval = new double[a.length];

		for (int i = 0; i < a.length; i++) {
			retval[i] = distanceBetweenPoints(a[i], b);
		}

		return retval;
	}

	// computes the distance in pixels between two points
	public static double distanceBetweenPoints(PointF a, PointF b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	// computes the minimum distance between a reference point and a set of
	// vertices
	public static double absDistanceToEdges(PointF[] vertices, PointF point) {
		double min_dist = distanceBetweenPoints(vertices[0], point);

		for (int a = 0; a < vertices.length; a++) {
			int b = (a + 1) % vertices.length;

			double dist = distanceBetweenPoints(nearestSegmentPoint(vertices[a], vertices[b], point), point);

			min_dist = Math.min(min_dist, dist);
		}

		return min_dist;
	}

	// calculates the point on the vertex defined by segStart and segEnd which
	// is closest
	// to the provided point
	public static PointF nearestSegmentPoint(PointF segStart, PointF segEnd, PointF point) {
		if (segStart == segEnd) {
			// Special case if the line segment is reduced to a single point.
			return segStart;
		}

		double dx = segEnd.x - segStart.x;
		double dy = segEnd.y - segStart.y;
		double ratio = (dx * (point.x - segStart.x) + dy * (point.y - segStart.y)) / (dx * dx + dy * dy);

		if (ratio < 0) {
			// The closest point of [segStart,segEnd] is segStart.
			return segStart;
		}
		if (ratio > 1) {
			// The closest point of [segStart,segEnd] is segEnd.
			return segEnd;
		}

		PointF a = new PointF((float) (segStart.x * (1.0 - ratio)), (float) (segStart.y * (1.0 - ratio)));
		PointF b = new PointF((float) (segEnd.x * ratio), (float) (segEnd.y * ratio));

		a.offset(b.x, b.y);

		// The closest point of [A,B] is between A and B.
		// return segStart * (1.0 - ratio) + segEnd * ratio;

		return a;
	}

	public static double angleBetweenVectors(PointF pivot, PointF first, PointF second) {
		double b = Math.pow(pivot.x - first.x, 2) + Math.pow(pivot.y - first.y, 2), a = Math.pow(pivot.x - second.x, 2)
				+ Math.pow(pivot.y - second.y, 2), c = Math.pow(second.x - first.x, 2)
				+ Math.pow(second.y - first.y, 2);

		return Math.toDegrees(Math.acos((a + b - c) / Math.sqrt(4 * a * b)));
	}

	public static int GetHashCode(String strValue) {
		CRC32 crc = new CRC32();
		crc.update(strValue.getBytes());
		return (int) (crc.getValue());
	}
	
	public static long GetCrc32Code(String filepath) throws IOException {

		InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
		CRC32 crc = new CRC32();
		int cnt;
		while ((cnt = inputStream.read()) != -1) {
			crc.update(cnt);
		}
		inputStream.close();
		return crc.getValue();
	}

	
	public static String GetMd5Code(String strFullFilename) {
		
		MessageDigest md;
		BufferedInputStream in = null;
		try {
			try {

				md = MessageDigest.getInstance("MD5");

				in = new BufferedInputStream(new FileInputStream(strFullFilename));

				int theByte = 0;
				while ((theByte = in.read()) != -1) {
					md.update((byte) theByte);
				}
				byte[] theDigest = md.digest();
				return ByteArray2String(theDigest);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String ByteArray2String(byte [] arrByte) {
		//converting byte array to Hexadecimal String 
		StringBuilder sb = new StringBuilder(2*arrByte.length);
		for(byte b : arrByte) { 
			sb.append(String.format("%02x", b&0xff)); 
		} 
		return sb.toString();
	}
	

	public static long getCurrentTimeInMilliSeconds() {
		long timestamp = System.currentTimeMillis();
		return timestamp;
	}

	@SuppressWarnings("unchecked")
	public static <T> T DeSerializeFromSharedPreferences(String strPrimaryKey, String strSecondaryKey,
			Context _context, Type objType) {

		SharedPreferences sharedPref = _context.getSharedPreferences(strPrimaryKey, Context.MODE_PRIVATE);
		String strSerialize = sharedPref.getString(strSecondaryKey, "");
		if (strSerialize.length() != 0) {
			Gson gson = new Gson();
			return (T) gson.fromJson(strSerialize, objType);
		}
		return null;
	}

	public static <T> void SerializeToSharedPreferences(String strPrimaryKey, String strSecondaryKey, Context _context,
			T obj) {
		Gson gson = new Gson();
		String strSerialized = gson.toJson(obj);
		SharedPreferences sharedPref = _context.getSharedPreferences(strPrimaryKey, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(strSecondaryKey, strSerialized);
		editor.commit();
		sharedPref = null;
	}

	@SuppressWarnings("unchecked")
	// Note: Here is example of getting type of ArrayList:
	// java.lang.reflect.Type collectionType = new
	// TypeToken<ArrayList<clsListViewState>>(){}.getType();
	public static <T> T DeSerializeFromString(String strSerialize, Type objType) {
		Gson gson = new Gson();
		return (T) gson.fromJson(strSerialize, objType);
	}

	public static void showKeyboard(Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	}

	public static void hideKeyboard(Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}

	public static int dpToPx(int dp, Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	// From
	// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	// with a slight change
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			while ((height / inSampleSize) > reqHeight && (width / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static <T> String SerializeToString(T obj) {
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	public static Date GetCurrentDateTime() {
		return new Date();
	}

	public static String GetStrCurrentDateTime() {
		return DateToJson(new Date());
	}
	
	public static String GetStrCurrentDateTimeWinFilenameSafe() {
		 String strValue = GetStrCurrentDateTime();
		 strValue = strValue.replaceAll("[^a-zA-Z0-9.-]", "_");
		 while (strValue.indexOf("__") != -1)
			 strValue = strValue.replace("__", "_");
		 return strValue;
	}

	public static String DateToJson(Date dtDate) {
		SimpleDateFormat df = new SimpleDateFormat(strDateTimeFormat);
		return df.format(dtDate);
	}

	public static Date JsonToDate(String strDate) {
		SimpleDateFormat df = new SimpleDateFormat(strDateTimeFormat);
		try {
			return df.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String DateToRfc822(Date dtDate) {
		String pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.US).format(dtDate);
		return pubDate;
	}
	
	public static Date Rfc822ToDate(String strDate) {
		SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.US);
		try {
			return df.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	

	public static boolean IsUserNameValid(String mUsername) {
		return true;
	}

	public static boolean IsUserNameValid2(String mUsername) {
		// TODO Auto-generated method stub
		int intSadFacePos = 0;
		int intCloseBracketPos = 1;
		intSadFacePos = mUsername.indexOf("<<");
		intCloseBracketPos = mUsername.indexOf(">>");
		if (intSadFacePos == 0) {
			if (intCloseBracketPos == mUsername.length() - 1) {
				return true;
			}
		}
		return false;
	}

	public static void CustomLog(String strMessage) {
		Log.d("myCustom", strMessage);
	}

	public static void SetMenuItemEnabled(MenuItem objMenuItem, boolean IsEnabled) {
		if (IsEnabled) {
			objMenuItem.setEnabled(true);
			objMenuItem.getIcon().setAlpha(255);
		} else {
			objMenuItem.setEnabled(false);
			objMenuItem.getIcon().setAlpha(130);
		}
	}

	public static <T> T CloneDeep(T objToBeCloned, Class<T> type) {
		String strClone = clsUtils.SerializeToString(objToBeCloned);
		T objClone = clsUtils.DeSerializeFromString(strClone, type);
		return type.cast(objClone);
	}

	public static Bitmap downsampleImageToView(String pathToFile, int viewWidth, int viewHeight, boolean mutable) {
		Bitmap out = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		// Get attributes of image without loading to memory
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(pathToFile, options);

		// calculate the sample size necessary to downscale the image
		options.inSampleSize = clsUtils.calculateInSampleSize(options, viewWidth, viewHeight);

		options.inJustDecodeBounds = false;

		options.inMutable = mutable;

		out = BitmapFactory.decodeFile(pathToFile, options);

		return out;
	}

	public static String getVideoPath(Context context, Uri uri) {
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		String document_id = cursor.getString(0);
		document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
		cursor.close();

		cursor = context.getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Video.Media._ID + " = ? ", new String[] { document_id }, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
		cursor.close();

		return path;
	}
	
	public static void ClearImageLoadDatas(ArrayList<clsImageLoadData> objImageLoadDatas) {
		for (clsImageLoadData objImageLoadData : objImageLoadDatas) {
			objImageLoadData.objImageToBeUploadedDatas.clear();
			objImageLoadData.strImagesToBeDownloaded.clear();
		}
		objImageLoadDatas.clear();
	}
	

	public static void UpdateImageLoadDatasForDownloads(clsMessaging objMessaging, clsGroupMembers objGroupMembers, clsNoteTreeview objNoteTreeview,
			File fileTreeNodesDir, ArrayList<clsImageLoadData> objServerReturnedImageLoadDatas, ArrayList<clsImageLoadData> objImageLoadDatas) {
		// Look for all images needed by note, if they already exist on client,
		// collect all the missing ones
		// Class that iterates
		class clsMyTreeviewIterator extends clsTreeviewIterator {

			clsImageLoadData objImageLoadData;
			File fileTreeNodesDir;
			clsTreeview objTreeview;
			clsGroupMembers objGroupMembers;

			public clsMyTreeviewIterator(clsGroupMembers objGroupMembers, clsTreeview objTreeview, File fileTreeNodesDir,
					clsImageLoadData objImageLoadData) {
				super(objTreeview);
				this.objImageLoadData = objImageLoadData;
				this.fileTreeNodesDir = fileTreeNodesDir;
				this.objTreeview = objTreeview;
				this.objGroupMembers = objGroupMembers;
			}

			@Override
			public void ProcessTreeNode(clsTreeNode objTreeNode, int intLevel) {
				// Only process nodes with images
				if (!objTreeNode.resourcePath.isEmpty()) {
					// Only process nodes from foreign users (item owner not the registered phone user) for downloads
					if (!objTreeNode.getStrOwnerUserUuid().equals(objGroupMembers.GetRegisteredUser().strUserUuid)) {
						// Foreign user nodes
						// If annotated image is missing locally, a download is definitely needed
						File fileImage = new File(fileTreeNodesDir + "/" + objTreeNode.guidTreeNode.toString() + "_annotated.jpg");
						if (!fileImage.exists() && objTreeNode.annotation != null) {
							// Annotated, but local file is missing
							// Add only unique items
							if (!objImageLoadData.strImagesToBeDownloaded.contains(objTreeNode.guidTreeNode.toString())) {
								objImageLoadData.strImagesToBeDownloaded.add(objTreeNode.guidTreeNode.toString());
							}
							return;
						}
						
						// Check only thumbnail for image config changes, other full file follow the thumbnail
						fileImage = new File(fileTreeNodesDir + "/" + objTreeNode.guidTreeNode.toString() + ".jpg");
						if (fileImage.exists()) {
							// File already exists client side, check if different image by comparing checksums
							String strLocalThumbnailChecksum = clsUtils.GetMd5Code(fileImage.getAbsolutePath());
							if (objTreeNode.objResourceGroupData.strThumbnailChecksum.equals(strLocalThumbnailChecksum) ) {
								// The same, so no need to update
							} else {
								// Different, if server version older than client, no need to upload, 
								 Date dtLocalFileModDate = new Date(fileImage.lastModified());
								 Date dtServerFileModDate = clsUtils.Rfc822ToDate(objTreeNode.objResourceGroupData.strThumbnailLastChanged);
								 if (dtLocalFileModDate.after(dtServerFileModDate)) {
									// Local file is newer (requires upload), which for a foreign owned note item is not possible
								 } else if (dtLocalFileModDate.before (dtServerFileModDate)) {
									// Local file older, so download required
									// Add only unique items
									if (!objImageLoadData.strImagesToBeDownloaded.contains(objTreeNode.guidTreeNode.toString())) {
										objImageLoadData.strImagesToBeDownloaded.add(objTreeNode.guidTreeNode.toString());
									}
								 } else {
										// Same age, so no upload or download required
								 }							
							}
						} else {
							// File does not exist client side, files definitely needs to be downloaded
							// Add only unique items
							if (!objImageLoadData.strImagesToBeDownloaded.contains(objTreeNode.guidTreeNode.toString())) {
								objImageLoadData.strImagesToBeDownloaded.add(objTreeNode.guidTreeNode.toString());
							}
						}
					}
				}
			}
		}

		// Do work here
		// Collect the items that needs to be downloaded (by checking local files against server provided config data)
		boolean boolAlreadyExists = false;
		clsImageLoadData objThisNoteImageLoadData = null;
		for (clsImageLoadData objImageLoadData : objImageLoadDatas) {
			// Append if structure already exists
			if (objImageLoadData.strNoteUuid.equals(objNoteTreeview.getRepository().uuidRepository)) {
				objThisNoteImageLoadData = objImageLoadData;
				clsMyTreeviewIterator objMyTreeviewIterator = new clsMyTreeviewIterator(objGroupMembers, objNoteTreeview,
						fileTreeNodesDir, objThisNoteImageLoadData);
				objMyTreeviewIterator.Execute();
				break;
			}
		}
		if (objThisNoteImageLoadData == null) {
			// Create new one if not existing yet
			objThisNoteImageLoadData = new clsImageLoadData();
			objThisNoteImageLoadData.strNoteUuid = objNoteTreeview.getRepository().uuidRepository.toString();
			objImageLoadDatas.add(objThisNoteImageLoadData);
			clsMyTreeviewIterator objMyTreeviewIterator = new clsMyTreeviewIterator(objGroupMembers, objNoteTreeview, fileTreeNodesDir,
					objThisNoteImageLoadData);
			objMyTreeviewIterator.Execute();
		}
		
		// If server detected and passed on a download is needed, add this here
		for (clsImageLoadData objServerReturnedImageLoadData: objServerReturnedImageLoadDatas) {
			for (String strImagesToBeDownloaded: objServerReturnedImageLoadData.strImagesToBeDownloaded) {
				// Add only unique items
				if (!objThisNoteImageLoadData.strImagesToBeDownloaded.contains(strImagesToBeDownloaded)) {
					objThisNoteImageLoadData.strImagesToBeDownloaded.add(strImagesToBeDownloaded);
				}
			}
		}

	}

	public static void UpdateImageLoadDatasForUploads(clsMessaging objMessaging,
			ArrayList<clsImageLoadData> objServerImageLoadDatas, ArrayList<clsImageLoadData> objClientImageLoadDatas) {
		// Merge the items that needs to be uploaded server with client (server
		// gave this info)

		for (clsImageLoadData objServerImageLoadData : objServerImageLoadDatas) {
			String strServerNoteUuid = objServerImageLoadData.strNoteUuid;
			// If note image data already exists on client side (ensure only unique and latest entry in table) ...
			boolean boolEntryExists = false;
			for (clsImageLoadData objClientImageLoadData : objClientImageLoadDatas) {
				if (objClientImageLoadData.strNoteUuid.equals(strServerNoteUuid)) {
					// There is an entry, so overwrite with latest report from
					// server
					objClientImageLoadData.objImageToBeUploadedDatas = objServerImageLoadData.objImageToBeUploadedDatas;
					boolEntryExists = true;
					break;
				}
			}
			if (!boolEntryExists) {
				// Does not exist, so create a new entry
				clsImageLoadData objClientImageLoadData = new clsImageLoadData();
				objClientImageLoadData.strNoteUuid = strServerNoteUuid;
				objClientImageLoadData.objImageToBeUploadedDatas = objServerImageLoadData.objImageToBeUploadedDatas;
				objClientImageLoadDatas.add(objClientImageLoadData);
			}
		}
	}

	public static void SendGmail(Activity activity, String subject, String text) {

		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/html");
		String[] toArr = new String[] { "someone@somewhere.com" };
		intent.putExtra(Intent.EXTRA_EMAIL, toArr);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(text));

		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			// handle error
		}
	}
	
	public static void ShareUrl(Activity activity, String subject, String text) {
		
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
	    share.setType("text/plain");
	    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	 
	    // Add data to the intent, the receiving app will decide
	    // what to do with it.
	    share.putExtra(Intent.EXTRA_SUBJECT, subject);
	    share.putExtra(Intent.EXTRA_TEXT, text);
	 
	    activity.startActivity(Intent.createChooser(share, "Share link!"));
			
	}

	public static int GetDefaultTabWidthInDp(Context objContext) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
		String strMaxIndentValue = sharedPref.getString("treenotes_default_user_indent_tab_width", "");
		if (strMaxIndentValue.isEmpty()) {
			int intDisplayMaxWidthInDp = clsUtils.GetDisplayMaxWidthInDp((Activity) objContext);
			int intMaxIndentAmount = ((Activity) objContext).getResources().getInteger(R.integer.indent_amount_max);
			int intMaxIndentValue = intDisplayMaxWidthInDp / intMaxIndentAmount;
			SharedPreferences.Editor objEditor = sharedPref.edit();
			objEditor.putString("treenotes_default_user_indent_tab_width", Integer.toString(intMaxIndentValue));
			objEditor.commit();
			return intMaxIndentValue;
		}
		return Integer.parseInt(strMaxIndentValue);
	}

	public static boolean IsLastCharacterFullstop(String strVal) {
		if (strVal.isEmpty()) return true;
		if (strVal.charAt(strVal.length() - 1) == '.') {
			return true;
		}
		return false;
	}

	public static String Unscramble(String base64EncodedPublicKeyScrambled) {
		// // For now, just flip the first "NUMBER" of bits around
		// final int NUMBER = 5;
		// String strBefore = base64EncodedPublicKeyScrambled.substring(0,
		// NUMBER);
		// String strAfter = "";
		// for (int intCharPos = strBefore.length()-1; intCharPos >= 0;
		// intCharPos-- ) {
		// strAfter += strBefore.charAt(intCharPos);
		// }
		// return base64EncodedPublicKeyScrambled.replaceFirst(strBefore,
		// strAfter);
		
		// Do nothing for now
		return base64EncodedPublicKeyScrambled;
	}

	public static clsIabLocalData LoadIabLocalValues(SharedPreferences sharedPref, clsIabLocalData objIabLocalData) {
		if (objIabLocalData == null) {
			objIabLocalData = new clsIabLocalData();
		}
		objIabLocalData.boolIsAdsDisabledA = sharedPref.getBoolean("treenotes_avalue", false);
		objIabLocalData.boolIsAdsDisabledB = sharedPref.getBoolean("treenotes_bvalue", true);
		return objIabLocalData;
	}

	public static void SaveIabLocalValues(SharedPreferences sharedPref, clsIabLocalData objIabLocalData) {
		/*
		 * WARNING: on a real application, we recommend you save data in a
		 * secure way to prevent tampering. For simplicity in this sample, we
		 * simply store the data using a SharedPreferences.
		 */
		if (objIabLocalData == null)
			return;
		SharedPreferences.Editor objEditor = sharedPref.edit();
		objEditor.putBoolean("treenotes_avalue", objIabLocalData.boolIsAdsDisabledA);
		objEditor.putBoolean("treenotes_bvalue", objIabLocalData.boolIsAdsDisabledB);
		objEditor.commit();
	}

	// Handle thumbnails, full and annotated images.
	public static String GetThumbnailImageFileName(Context context, String strTreeNodeUuid) {
		String result = GetTreeNotesDirectoryName(context);

		result = result + "/" + strTreeNodeUuid + ".jpg";

		return result;
	}

	public static String GetFullImageFileName(Context context, String strTreeNodeUuid) {
		String result = GetTreeNotesDirectoryName(context);

		result = result + "/" + strTreeNodeUuid + "_full.jpg";

		return result;
	}

	public static String GetAnnotatedImageFileName(Context context, String strTreeNodeUuid) {
		String result = GetTreeNotesDirectoryName(context);

		result = result + "/" + strTreeNodeUuid + "_annotated.jpg";

		return result;
	}

	public static void CreateBackupFromFile(File origFile) {
		if (origFile.exists()) {
			File backupFile = new File(origFile + ".backup");
			CopyFile(origFile, backupFile, false);
		}
	}

	public static void RestoreFileFromBackup(File origFile) {
		File backupFile = new File(origFile + ".backup");
		if (backupFile.exists()) {
			backupFile.renameTo(origFile);
			backupFile.delete();
		}
	}

	public static void RemoveBackupImagesOfNode(Context context, final String strTreeNodeUuid) {
		File dir = new File(GetTreeNotesDirectoryName(context));

		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(strTreeNodeUuid) && filename.contains(".backup")) {
					return true;
				}
				return false;
			}
		});

		if (files != null) {
			for (File fp : files) {
				if (fp.exists()) {
					fp.delete();
				}
			}
		}
	}

	public static void RemoveAllImagesOfNode(Context context, final String strTreeNodeUuid) {
		File dir = new File(GetTreeNotesDirectoryName(context));

		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(strTreeNodeUuid)) {
					return true;
				}
				return false;
			}
		});

		if (files != null) {
			for (File fp : files) {
				if (fp.exists()) {
					fp.delete();
				}
			}
		}
	}

	// Determine if app runs on emulator
	public static boolean RunsOnEmu(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;

		return "generic".endsWith(Build.BRAND.toLowerCase(locale));
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public static String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(ActivityExplorerStartup.PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(ActivityExplorerStartup.TAG_GCM, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(ActivityExplorerStartup.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(ActivityExplorerStartup.TAG_GCM, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	public static SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(ActivityExplorerStartup.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	public static void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(ActivityExplorerStartup.TAG_GCM, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(ActivityExplorerStartup.PROPERTY_REG_ID, regId);
		editor.putInt(ActivityExplorerStartup.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public static String GetFileLastModifiedDate(String strFile) {
		File file = new File(strFile);
		Date lastModDate = new Date(file.lastModified());
		return DateToRfc822(lastModDate);
	}
	
	
}
