package com.vulpovile.games.brickblaster.util;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.filechooser.FileFilter;

public class FileExtentionFilter extends FileFilter{
	private final HashSet<String> extentions = new HashSet<String>();
	private final String desc;
	
	public FileExtentionFilter(String desc, String ... extentions)
	{
		this.extentions.addAll(Arrays.asList(extentions));
		StringBuilder sb = new StringBuilder();
		sb.append(desc);
		sb.append(" (");
		for(int i = 0; i < extentions.length; i++)
		{
			sb.append("*.");
			sb.append(extentions[i]);
			if(extentions.length > i+1)
			{
				sb.append(" | ");
			}
		}
		sb.append(")");
		this.desc = sb.toString();
	}
	
	@Override
	public boolean accept(File arg0) {
		if(arg0.isDirectory())
			return true;
		int idx = arg0.getName().lastIndexOf(".")+1;
		if(idx == 0 || idx == arg0.getName().length())
			return false;
		return extentions.contains(arg0.getName().substring(idx).toLowerCase());
	}

	@Override
	public String getDescription() {
		return desc;
	}
	
}
