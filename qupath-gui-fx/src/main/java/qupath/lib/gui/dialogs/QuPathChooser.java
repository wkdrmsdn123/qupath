/*-
 * #%L
 * This file is part of QuPath.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * Copyright (C) 2018 - 2020 QuPath developers, The University of Edinburgh
 * %%
 * QuPath is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * QuPath is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with QuPath.  If not, see <https://www.gnu.org/licenses/>.
 * #L%
 */

package qupath.lib.gui.dialogs;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * Interface to help with prompting users to select files or directories.
 * The intension is that sub-classes might use different kinds of (possibly platform-specific) file choosers.
 * <p>
 * In truth, having this as an interface made most sense back in the Swing days - whenever it was often the 
 * case that the Swing and AWT dialogs were quite different in appearance (and pleasantness).
 * <p>
 * With JavaFX that's not so important - however it is maintained as an interface in case alternative dialogs 
 * might be introduced in the future.
 * 
 * @author Pete Bankhead
 *
 */
public interface QuPathChooser {

	/**
	 * Set the last directory, which will be used as the base directory in future file choosers.
	 * <p>
	 * If a file (not directory) is given, the parent directory will be used.
	 * If the file does not exist, the last directory will be set to null.
	 * 
	 * @param dir
	 */
	public void setLastDirectory(File dir);

	/**
	 * Get the last directory, which may be used as the starting directory the next time the chooser is shown 
	 * if no other directory is specified.
	 * 
	 * @return
	 */
	public File getLastDirectory();
	
	/**
	 * Prompt to open a list of files.
	 * 
	 * @param title
	 * @param dirBase
	 * @param filterDescription
	 * @param exts
	 * @return
	 */
	public List<File> promptForMultipleFiles(String title, File dirBase, String filterDescription, String... exts);
	
	/**
	 * Prompt user to select a directory.
	 * 
	 * @param dirBase base directory to display; if null or not an existing directory, the value under getLastDirectory() should be used
	 * @return selected directory, or null if no directory was selected
	 */
	public abstract File promptForDirectory(File dirBase);

	/**
	 * Prompt the user for a file with some kind of file dialog.
	 * @param title the title to display for the dialog (may be null to use default)
	 * @param dirBase base directory to display; if null or not an existing directory, the value under getLastDirectory() should be used
	 * @param filterDescription description to (possibly) show for the file name filter (may be null if no filter should be used)
	 * @param exts optional array of file extensions if filterDescription is not null
	 * 
	 * @return the File selected by the user, or null if the dialog was cancelled
	 */
	public abstract File promptForFile(String title, File dirBase, String filterDescription, String... exts);

	/**
	 * Prompt user to select a file.
	 * 
	 * @param dirBase base directory to display; if null or not an existing directory, the value under getLastDirectory() should be used
	 * @return the File selected by the user, or null if the dialog was cancelled
	 */
	public File promptForFile(File dirBase);
	
	/**
	 * Prompt user to select a file path to save, using a single file extension as an option.
	 * 
	 * @param title the title to display for the dialog (may be null)
	 * @param dirBase the base directory to display; if null or not an existing directory, the value under getLastDirectory() should be used
	 * @param defaultName default file name
	 * @param filterDescription description to show for the file name filter (may be null if no filter should be used)
	 * @param ext extension that should be used for the saved file (may be empty or null if not specified)
	 * @return the File selected by the user, or null if the dialog was cancelled
	 */
	public File promptToSaveFile(String title, File dirBase, String defaultName, String filterDescription, String ext);
	
	/**
	 * Prompt user to select a file path to save, providing zero or more file extensions as an option.
	 * 
	 * @param title the title to display for the dialog (may be null)
	 * @param dirBase the base directory to display; if null or not an existing directory, the value under getLastDirectory() should be used
	 * @param defaultName default file name
	 * @param filters map of file type descriptions (keys) and file extensions (values); may be empty if an 'all files' filter should be used
	 * @return the File selected by the user, or null if the dialog was cancelled
	 * @since v0.4.0
	 */
	public File promptToSaveFile(String title, File dirBase, String defaultName, Map<String, String> filters);
	
	/**
	 * Prompt user to select a file or input a URL.
	 * 
	 * @param title dialog title
	 * @param defaultPath default path to display - may be null
	 * @param dirBase base directory to display; if null or not an existing directory, the value under getLastDirectory() should be used
	 * @param filterDescription description to (possibly) show for the file name filter (may be null if no filter should be used)
	 * @param exts optional array of file extensions if filterDescription is not null
	 * @return the path to the file or URL, or null if no path was provided.
	 */
	public String promptForFilePathOrURL(String title, String defaultPath, File dirBase, String filterDescription, String... exts);
	
}
