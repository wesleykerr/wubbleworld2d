package edu.arizona.simulator.ww2d.utils;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;

public class Compressor {

	public static void main(String[] args) { 
		String input = "states/run-1271404748315/";
		IMediaReader reader = ToolFactory.makeReader(input + "movie-agent1.mp4");
		
		IMediaWriter writer = ToolFactory.makeWriter(input + "movie-agent1.mov");
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, IRational.make(1,30), 800, 600);

		reader.addListener(writer);

		while (reader.readPacket() == null);
		
	}
}
