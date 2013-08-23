/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <ExternalDevice.java> is part of Minimal Dicom Viewer.
 *
 * Minimal Dicom Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minimal Dicom Viewer is distributed as Open Source Software ( OSS )
 * and comes WITHOUT ANY WARRANTY and even with no IMPLIED WARRANTIES OF MERCHANTABILITY,
 * OF SATISFACTORY QUALITY, AND OF FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License ( GPLv3 ) for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Minimal Dicom Viewer. If not, see <http://www.gnu.org/licenses/>.
 *
 * Released date: 13-11-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

import android.os.Environment;

public class ExternalDevice {

	// check availability of ext device
	public static boolean isExternalDeviceAvailable() {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

}
