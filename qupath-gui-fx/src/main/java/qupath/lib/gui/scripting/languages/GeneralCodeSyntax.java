/*-
 * #%L
 * This file is part of QuPath.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * Copyright (C) 2018 - 2022 QuPath developers, The University of Edinburgh
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

package qupath.lib.gui.scripting.languages;

import javafx.scene.control.IndexRange;
import qupath.lib.gui.scripting.ScriptEditorControl;

/**
 * Abstract class to represent the typical syntaxes found in most programming languages.
 * @author Melvin Gelbard
 * @since v0.4.0
 */
abstract class GeneralCodeSyntax implements ScriptSyntax {

	/**
	 * Handle left parentheses '{@code (}' by automatically adding a right parenthesis '{@code )}' after if not already present.
	 */
	@Override
	public void handleLeftParenthesis(final ScriptEditorControl control, final boolean smartEditing) {
		control.insertText(control.getCaretPosition(), "(");
		if (!smartEditing)
			return;
		control.insertText(control.getCaretPosition(), ")");
		control.positionCaret(control.getCaretPosition()-1);
	}
	
	/**
	 * Adds a right parenthesis '{@code )}', except if one is already present at the caret position.
	 */
	@Override
	public void handleRightParenthesis(final ScriptEditorControl control, final boolean smartEditing) {
		control.insertText(control.getCaretPosition(), ")");
		if (!smartEditing)
			return;
		
		String text = control.getText();
		var caretPos = control.getCaretPosition();
		if (text.length() >= caretPos + 1 && text.charAt(caretPos) == ')') {
			control.deleteText(caretPos, caretPos + 1);
		}
	}
	
	/**
	 * Handle (single/double) quotes by adding a closing one if not already present.
	 */
	@Override
	public void handleQuotes(final ScriptEditorControl control, boolean isDoubleQuote, final boolean smartEditing) {
		String quotes = isDoubleQuote ? "\"" : "\'";
		control.insertText(control.getCaretPosition(), quotes);
		if (!smartEditing)
			return;
		
		String text = control.getText();
		var caretPos = control.getCaretPosition();
		if (text.length() >= caretPos + 1 && text.charAt(caretPos) == quotes.charAt(0))
			control.deleteText(caretPos, caretPos + 1);
		else {
			control.insertText(control.getCaretPosition(), quotes);
			control.positionCaret(control.getCaretPosition()-1);
		}
	}
	
	/**
	 * Handle the press of the / key, with/without shift.
	 * This either inserts comments or uncomments the selected lines, if possible.
	 */
	@Override
	public void handleLineComment(final ScriptEditorControl control) {
		String commentString = getLineCommentString();
		if (commentString == null)
			return;

		String text = control.getText();
		IndexRange range = control.getSelection();
		boolean hasSelection = range.getLength() > 0;
		int startRowPos = getRowStartPosition(text, range.getStart());
		int endRowPos = getRowEndPosition(text, range.getEnd());
		String textBetween = text.substring(startRowPos, endRowPos);
		// Check if every new row starts with a comment string - if so we want to remove these, if not we want to add comments
		
		int nNewLines = textBetween.length() - textBetween.replace("\n", "").length();
		int nCommentLines = (textBetween.length() - textBetween.replace("\n" + commentString, commentString).length());
		boolean allComments = textBetween.startsWith(commentString) && nNewLines == nCommentLines;
		
		String replaceText;
		if (allComments) {
			// Remove tabs at start of selected rows
			replaceText = textBetween.replace("\n"+commentString, "\n");
			if (replaceText.startsWith(commentString))
				replaceText = replaceText.substring(commentString.length());
		} else {
			replaceText = commentString + textBetween.replace("\n", "\n"+commentString);
		}
		
		control.selectRange(startRowPos, endRowPos);
		control.paste(replaceText);
		if (hasSelection)
			control.selectRange(startRowPos, startRowPos + replaceText.length());
	}
	
	@Override
	public void handleNewLine(ScriptEditorControl control, final boolean smartEditing) {
		String text = control.getText();
		int caretPos = control.getCaretPosition();
		int startRowPos = getRowStartPosition(text, caretPos);
		String subString = text.substring(startRowPos, caretPos);
		String indentation = subString.substring(0, subString.length() - subString.stripLeading().length());
		ScriptSyntax.super.handleNewLine(control, smartEditing);
		if (smartEditing)
			control.insertText(control.getCaretPosition(), indentation);
	}

	/**
	 * Handle backspace if required, otherwise does nothing (and let the original control deal with the backspace).
	 * <p>
	 * This was implemented this way because there's no point in rewriting all the rules for backspace 
	 * (e.g. {@code SHORTCUT} + {@code BACKSPACE}, {@code BACKSPACE} on a selection, etc..).
	 */
	@Override
	public boolean handleBackspace(final ScriptEditorControl control, final boolean smartEditing) {
		var caretPos = control.getCaretPosition();
		var selection = control.getSelection();
		if (caretPos -1 < 0 || selection.getLength() >= 1 || !smartEditing)
			return false;
		
		if (caretPos >= control.getText().length() ||
				(!(control.getText().charAt(caretPos-1) == '(' && control.getText().charAt(caretPos) == ')') &&
				!(control.getText().charAt(caretPos-1) == '"' && control.getText().charAt(caretPos) == '"') &&
				!(control.getText().charAt(caretPos-1) == '\'' && control.getText().charAt(caretPos) == '\'')))
			return false;
		
		control.deleteText(caretPos-1, caretPos+1);
		return true;
	}
	
	/**
	 * Handle the press of the tab key, with/without shift.
	 * <p>
	 * If there is no selection :
	 * <li> It inserts a {@code tabString} at the current caret position if shift is not down </li>
	 * <li> It removes a {@code tabString} at the start of the line if there is one and if shift is down </li>
	 * <p>
	 * If there is a selection:
	 * <li> It indents all the selected rows if shift is not down </li>
	 * <li> It removes one indentation from all the selected rows if shift is down </li>
	 */
	@Override
	public void handleTabPress(final ScriptEditorControl textArea, final boolean shiftDown) {
		String text = textArea.getText();
		IndexRange range = textArea.getSelection() == null ? IndexRange.valueOf("0,0") : textArea.getSelection();
		int startRowPos = getRowStartPosition(text, range.getStart());
		int endRowPos = getRowEndPosition(text, range.getEnd());
		String textBetween = text.substring(startRowPos, endRowPos);

		if (range.getLength() == 0) {
			int caretPos = textArea.getCaretPosition();
			if (shiftDown && textBetween.indexOf(tabString) == 0) {
				textArea.deleteText(startRowPos, startRowPos + tabString.length());
				textArea.positionCaret(caretPos - tabString.length());
			} else if (!shiftDown)
				textArea.insertText(caretPos, tabString);
			return;
		}

		String replaceText;
		if (shiftDown) {
			// Remove tabs at start of selected rows
			replaceText = textBetween.replace("\n"+tabString, "\n");
			if (replaceText.startsWith(tabString))
				replaceText = replaceText.substring(tabString.length());
		} else
			replaceText = tabString + textBetween.replace("\n", "\n"+tabString);
		
		textArea.selectRange(startRowPos, endRowPos);
		textArea.paste(replaceText);
		textArea.selectRange(startRowPos, startRowPos + replaceText.length());
	}
	
	static int getRowStartPosition(final String text, final int pos) {
		return text.substring(0, pos).lastIndexOf("\n") + 1;
	}

	static int getRowEndPosition(final String text, final int pos) {
		int pos2 = text.substring(pos).indexOf("\n");
		if (pos2 < 0)
			return text.length();
		return pos + pos2;
	}
}
