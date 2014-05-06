/******************************************************************************
 * Spine Runtimes Software License
 * Version 2
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software, you may not (a) modify, translate, adapt or
 * otherwise create derivative works, improvements of the Software or develop
 * new applications using the Software or (b) remove, delete, alter or obscure
 * any trademarks or any copyright, trademark, patent or other intellectual
 * property or proprietary rights notices on or in the Software, including
 * any copy thereof. Redistributions in binary or source form must include
 * this license and terms. THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.esotericsoftware.spine.Animation.AttachmentTimeline;
import com.esotericsoftware.spine.Animation.ColorTimeline;
import com.esotericsoftware.spine.Animation.CurveTimeline;
import com.esotericsoftware.spine.Animation.DrawOrderTimeline;
import com.esotericsoftware.spine.Animation.EventTimeline;
import com.esotericsoftware.spine.Animation.FfdTimeline;
import com.esotericsoftware.spine.Animation.RotateTimeline;
import com.esotericsoftware.spine.Animation.ScaleTimeline;
import com.esotericsoftware.spine.Animation.Timeline;
import com.esotericsoftware.spine.Animation.TranslateTimeline;
import com.esotericsoftware.spine.attachments.AtlasAttachmentLoader;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.AttachmentLoader;
import com.esotericsoftware.spine.attachments.AttachmentType;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.esotericsoftware.spine.attachments.SkinnedMeshAttachment;

public class SkeletonBinary {
	static public final int TIMELINE_SCALE = 0;
	static public final int TIMELINE_ROTATE = 1;
	static public final int TIMELINE_TRANSLATE = 2;
	static public final int TIMELINE_ATTACHMENT = 3;
	static public final int TIMELINE_COLOR = 4;
	static public final int TIMELINE_EVENT = 5;
	static public final int TIMELINE_DRAWORDER = 6;
	static public final int TIMELINE_FFD = 7;

	static public final int CURVE_LINEAR = 0;
	static public final int CURVE_STEPPED = 1;
	static public final int CURVE_BEZIER = 2;

	private final AttachmentLoader attachmentLoader;
	private float scale = 1;

	public SkeletonBinary (Atlas atlas) {
		attachmentLoader = new AtlasAttachmentLoader(atlas);
	}

	public SkeletonBinary (AttachmentLoader attachmentLoader) {
		this.attachmentLoader = attachmentLoader;
	}

	public float getScale () {
		return scale;
	}

	public void setScale (float scale) {
		this.scale = scale;
	}

	public SkeletonData readSkeletonData (String sname, InputStream inputStream) {
		if (sname == null) throw new IllegalArgumentException("sname cannot be null.");
		if (inputStream == null) throw new IllegalArgumentException("inputStream cannot be null.");

		float scale = this.scale;

		SkeletonData skeletonData = new SkeletonData();
		skeletonData.name = sname;

		DataInput input = new DataInput(inputStream);

		try {
			boolean nonessential = input.readBoolean();
			
			// Bones.
			for (int i = 0, n = input.readInt(true); i < n; i++) {
				String name = input.readString();
				BoneData parent = null;
				int parentIndex = input.readInt(true) - 1;
				if (parentIndex != -1) parent = skeletonData.bones.get(parentIndex);
				BoneData boneData = new BoneData(name, parent);
				boneData.x = input.readFloat() * scale;
				boneData.y = input.readFloat() * scale;
				boneData.scaleX = input.readFloat();
				boneData.scaleY = input.readFloat();
				boneData.rotation = input.readFloat();
				boneData.length = input.readFloat() * scale;
				boneData.inheritScale = input.readBoolean();
				boneData.inheritRotation = input.readBoolean();
				if (nonessential) {
					int color = input.readInt();
					boneData.r = ((color) >>> 24) / 255f;
					boneData.g = ((color & 0x00ff0000) >>> 16) / 255f;
					boneData.b = ((color & 0x0000ff00) >>> 8) / 255f;
					boneData.a = ((color & 0x000000ff)) / 255f;
				}
				skeletonData.addBone(boneData);
			}

			// Slots.
			for (int i = 0, n = input.readInt(true); i < n; i++) {
				String slotName = input.readString();
				BoneData boneData = skeletonData.bones.get(input.readInt(true));
				SlotData slotData = new SlotData(slotName, boneData);
				int color = input.readInt();
				slotData.r = ((color) >>> 24) / 255f;
				slotData.g = ((color & 0x00ff0000) >>> 16) / 255f;
				slotData.b = ((color & 0x0000ff00) >>> 8) / 255f;
				slotData.a = ((color & 0x000000ff)) / 255f;
				slotData.attachmentName = input.readString();
				slotData.additiveBlending = input.readBoolean();
				skeletonData.addSlot(slotData);
			}

			// Default skin.
			Skin defaultSkin = readSkin(input, "default", nonessential);
			if (defaultSkin != null) {
				skeletonData.defaultSkin = defaultSkin;
				skeletonData.addSkin(defaultSkin);
			}

			// Skins.
			for (int i = 0, n = input.readInt(true); i < n; i++)
				skeletonData.addSkin(readSkin(input, input.readString(), nonessential));

			// Events.
			for (int i = 0, n = input.readInt(true); i < n; i++) {
				EventData eventData = new EventData(input.readString());
				eventData.intValue = input.readInt(false);
				eventData.floatValue = input.readFloat();
				eventData.stringValue = input.readString();
				skeletonData.addEvent(eventData);
			}

			// Animations.
			for (int i = 0, n = input.readInt(true); i < n; i++)
				readAnimation(input.readString(), input, skeletonData);

		} catch (IOException ex) {
			throw new RuntimeException("Error reading skeleton file.", ex);
		} finally {
			try {
				input.close();
			} catch (IOException ignored) {
			}
		}

		skeletonData.bones.trimToSize();
		skeletonData.slots.trimToSize();
		skeletonData.skins.trimToSize();
		return skeletonData;
	}

	private Skin readSkin (DataInput input, String skinName, boolean nonessential) throws IOException {
		int slotCount = input.readInt(true);
		if (slotCount == 0) return null;
		Skin skin = new Skin(skinName);
		for (int i = 0; i < slotCount; i++) {
			int slotIndex = input.readInt(true);
			for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
				String name = input.readString();
				skin.addAttachment(slotIndex, name, readAttachment(input, skin, name, nonessential));
			}
		}
		return skin;
	}

	private Attachment readAttachment (DataInput input, Skin skin, String attachmentName, boolean nonessential) throws IOException {
		float scale = this.scale;

		String name = input.readString();
		if (name == null) name = attachmentName;

		switch (AttachmentType.values()[input.readByte()]) {
		case region: {
			String path = input.readString();
			if (path == null) path = name;
			RegionAttachment region = attachmentLoader.newRegionAttachment(skin, name, path);
			if (region == null) return null;
			region.setX(input.readFloat() * scale);
			region.setY(input.readFloat() * scale);
			region.setScaleX(input.readFloat());
			region.setScaleY(input.readFloat());
			region.setRotation(input.readFloat());
			region.setWidth(input.readFloat() * scale);
			region.setHeight(input.readFloat() * scale);
			int color = input.readInt();
			region.setR(((color) >>> 24) / 255f);
			region.setG(((color & 0x00ff0000) >>> 16) / 255f);
			region.setB(((color & 0x0000ff00) >>> 8) / 255f);
			region.setA(((color & 0x000000ff)) / 255f);
			region.updateOffset();
			return region;
		}
		case boundingbox: {
			BoundingBoxAttachment box = attachmentLoader.newBoundingBoxAttachment(skin, name);
			if (box == null) return null;
			box.setVertices(readFloatArray(input, scale));
			return box;
		}
		case mesh: {
			String path = input.readString();
			if (path == null) path = name;
			MeshAttachment mesh = attachmentLoader.newMeshAttachment(skin, name, path);
			float[] uvs = readFloatArray(input, 1);
			short[] triangles = readShortArray(input);
			float[] vertices = readFloatArray(input, scale);
			mesh.setVertices(vertices);
			mesh.setTriangles(triangles);
			mesh.setRegionUVs(uvs);
			mesh.updateUVs();
			int color = input.readInt();
			mesh.setR(((color) >>> 24) / 255f);
			mesh.setG(((color & 0x00ff0000) >>> 16) / 255f);
			mesh.setB(((color & 0x0000ff00) >>> 8) / 255f);
			mesh.setA(((color & 0x000000ff)) / 255f);
			if (nonessential) {
				mesh.setEdges(readIntArray(input));
				mesh.setHullLength(input.readInt(true) * 2);
				mesh.setWidth(input.readFloat() * scale);
				mesh.setHeight(input.readFloat() * scale);
			}
			return mesh;
		}
		case skinnedmesh: {
			String path = input.readString();
			if (path == null) path = name;
			SkinnedMeshAttachment mesh = attachmentLoader.newSkinnedMeshAttachment(skin, name, path);
			float[] uvs = readFloatArray(input, 1);
			short[] triangles = readShortArray(input);

			int vertexCount = input.readInt(true);
			ArrayList<Float> weights = new ArrayList<Float>(uvs.length * 3 * 3);
			ArrayList<Integer> bones = new ArrayList<Integer>(uvs.length * 3);
			for (int i = 0, n = vertexCount; i < n; ) {
				int boneCount = (int) input.readFloat();
				bones.add(boneCount);
				for (int nn = i + boneCount * 4; i < nn; ) {
					bones.add((int)input.readFloat());
					weights.add(input.readFloat() * scale);
					weights.add(input.readFloat() * scale);
					weights.add(input.readFloat());
					i += 4;
				}
			}
			int[] ibones = new int[bones.size()];
			for (int i = 0; i < ibones.length; i++) {
				ibones[i] = bones.get(i);
			}
			mesh.setBones(ibones);
			float[] fweights = new float[weights.size()];
			for (int i = 0; i < fweights.length; i++) {
				fweights[i] = weights.get(i);
			}
			mesh.setWeights(fweights);
			mesh.setTriangles(triangles);
			mesh.setRegionUVs(uvs);
			mesh.updateUVs();
			int color = input.readInt();
			mesh.setR(((color) >>> 24) / 255f);
			mesh.setG(((color & 0x00ff0000) >>> 16) / 255f);
			mesh.setB(((color & 0x0000ff00) >>> 8) / 255f);
			mesh.setA(((color & 0x000000ff)) / 255f);
			if (nonessential) {
				mesh.setEdges(readIntArray(input));
				mesh.setHullLength(input.readInt(true) * 2);
				mesh.setWidth(input.readFloat() * scale);
				mesh.setHeight(input.readFloat() * scale);
			}
			return mesh;
		}
		}
		return null;
	}

	private float[] readFloatArray (DataInput input, float scale) throws IOException {
		int n = input.readInt(true);
		float[] array = new float[n];
		if (scale == 1) {
			for (int i = 0; i < n; i++)
				array[i] = input.readFloat();
		} else {
			for (int i = 0; i < n; i++)
				array[i] = input.readFloat() * scale;
		}
		return array;
	}

	private short[] readShortArray (DataInput input) throws IOException {
		int n = input.readInt(true);
		short[] array = new short[n];
		for (int i = 0; i < n; i++)
			array[i] = input.readShort();
		return array;
	}

	private int[] readIntArray (DataInput input) throws IOException {
		int n = input.readInt(true);
		int[] array = new int[n];
		for (int i = 0; i < n; i++)
			array[i] = input.readInt(true);
		return array;
	}

	private void readAnimation (String name, DataInput input, SkeletonData skeletonData) {
		ArrayList<Timeline> timelines = new ArrayList<Timeline>();
		float scale = this.scale;
		float duration = 0;

		try {
			// Slot timelines.
			for (int i = 0, n = input.readInt(true); i < n; i++) {
				int slotIndex = input.readInt(true);
				for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
					int timelineType = input.readByte();
					int frameCount = input.readInt(true);
					switch (timelineType) {
					case TIMELINE_COLOR: {
						ColorTimeline timeline = new ColorTimeline(frameCount);
						timeline.slotIndex = slotIndex;
						for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
							float time = input.readFloat();
							int color = input.readInt();
							float r = ((color) >>> 24) / 255f;
							float g = ((color & 0x00ff0000) >>> 16) / 255f;
							float b = ((color & 0x0000ff00) >>> 8) / 255f;
							float a = ((color & 0x000000ff)) / 255f;
							timeline.setFrame(frameIndex, time, r, g, b, a);
							if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[frameCount * 5 - 5]);
						break;
					}
					case TIMELINE_ATTACHMENT:
						AttachmentTimeline timeline = new AttachmentTimeline(frameCount);
						timeline.slotIndex = slotIndex;
						for (int frameIndex = 0; frameIndex < frameCount; frameIndex++)
							timeline.setFrame(frameIndex, input.readFloat(), input.readString());
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[frameCount - 1]);
						break;
					}
				}
			}

			// Bone timelines.
			for (int i = 0, n = input.readInt(true); i < n; i++) {
				int boneIndex = input.readInt(true);
				for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
					int timelineType = input.readByte();
					int frameCount = input.readInt(true);
					switch (timelineType) {
					case TIMELINE_ROTATE: {
						RotateTimeline timeline = new RotateTimeline(frameCount);
						timeline.boneIndex = boneIndex;
						for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
							timeline.setFrame(frameIndex, input.readFloat(), input.readFloat());
							if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[frameCount * 2 - 2]);
						break;
					}
					case TIMELINE_TRANSLATE:
					case TIMELINE_SCALE:
						TranslateTimeline timeline;
						float timelineScale = 1;
						if (timelineType == TIMELINE_SCALE)
							timeline = new ScaleTimeline(frameCount);
						else {
							timeline = new TranslateTimeline(frameCount);
							timelineScale = scale;
						}
						timeline.boneIndex = boneIndex;
						for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
							timeline.setFrame(frameIndex, input.readFloat(), input.readFloat() * timelineScale, input.readFloat()
								* timelineScale);
							if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[frameCount * 3 - 3]);
						break;
					}
				}
			}

			// FFD timelines.
			for (int i = 0, n = input.readInt(true); i < n; i++) {
				Skin skin = skeletonData.getSkins().get(input.readInt(true) + 1);
				for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
					int slotIndex = input.readInt(true);
					for (int iii = 0, nnn = input.readInt(true); iii < nnn; iii++) {
						Attachment attachment = skin.getAttachment(slotIndex, input.readString());
						int frameCount = input.readInt(true);
						FfdTimeline timeline = new FfdTimeline(frameCount);
						timeline.slotIndex = slotIndex;
						timeline.attachment = attachment;
						for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
							float time = input.readFloat();

							float[] vertices;
							int vertexCount;
							if (attachment instanceof MeshAttachment)
								vertexCount = ((MeshAttachment)attachment).getVertices().length;
							else
								vertexCount = ((SkinnedMeshAttachment)attachment).getWeights().length / 3 * 2;

							int end = input.readInt(true);
							if (end == 0) {
								if (attachment instanceof MeshAttachment)
									vertices = ((MeshAttachment)attachment).getVertices();
								else
									vertices = new float[vertexCount];
							} else {
								vertices = new float[vertexCount];
								int start = input.readInt(true);
								end += start;
								if (scale == 1) {
									for (int v = start; v < end; v++)
										vertices[v] = input.readFloat();
								} else {
									for (int v = start; v < end; v++)
										vertices[v] = input.readFloat() * scale;
								}
								if (attachment instanceof MeshAttachment) {
									float[] meshVertices = ((MeshAttachment)attachment).getVertices();
									for (int v = 0, vn = vertices.length; v < vn; v++)
										vertices[v] += meshVertices[v];
								}
							}

							timeline.setFrame(frameIndex, time, vertices);
							if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[frameCount - 1]);
					}
				}
			}

			// Draw order timeline.
			int drawOrderCount = input.readInt(true);
			if (drawOrderCount > 0) {
				DrawOrderTimeline timeline = new DrawOrderTimeline(drawOrderCount);
				int slotCount = skeletonData.slots.size();
				for (int i = 0; i < drawOrderCount; i++) {
					int offsetCount = input.readInt(true);
					int[] drawOrder = new int[slotCount];
					for (int ii = slotCount - 1; ii >= 0; ii--)
						drawOrder[ii] = -1;
					int[] unchanged = new int[slotCount - offsetCount];
					int originalIndex = 0, unchangedIndex = 0;
					for (int ii = 0; ii < offsetCount; ii++) {
						int slotIndex = input.readInt(true);
						// Collect unchanged items.
						while (originalIndex != slotIndex)
							unchanged[unchangedIndex++] = originalIndex++;
						// Set changed items.
						drawOrder[originalIndex + input.readInt(true)] = originalIndex++;
					}
					// Collect remaining unchanged items.
					while (originalIndex < slotCount)
						unchanged[unchangedIndex++] = originalIndex++;
					// Fill in unchanged items.
					for (int ii = slotCount - 1; ii >= 0; ii--)
						if (drawOrder[ii] == -1) drawOrder[ii] = unchanged[--unchangedIndex];
					timeline.setFrame(i, input.readFloat(), drawOrder);
				}
				timelines.add(timeline);
				duration = Math.max(duration, timeline.getFrames()[drawOrderCount - 1]);
			}

			// Event timeline.
			int eventCount = input.readInt(true);
			if (eventCount > 0) {
				EventTimeline timeline = new EventTimeline(eventCount);
				for (int i = 0; i < eventCount; i++) {
					float time = input.readFloat();
					EventData eventData = skeletonData.events.get(input.readInt(true));
					Event event = new Event(eventData);
					event.intValue = input.readInt(false);
					event.floatValue = input.readFloat();
					event.stringValue = input.readBoolean() ? input.readString() : eventData.stringValue;
					timeline.setFrame(i, time, event);
				}
				timelines.add(timeline);
				duration = Math.max(duration, timeline.getFrames()[eventCount - 1]);
			}
		} catch (IOException ex) {
			throw new RuntimeException("Error reading skeleton file.", ex);
		}

		timelines.trimToSize();
		skeletonData.addAnimation(new Animation(name, timelines, duration));
	}

	private void readCurve (DataInput input, int frameIndex, CurveTimeline timeline) throws IOException {
		switch (input.readByte()) {
		case CURVE_STEPPED:
			timeline.setStepped(frameIndex);
			break;
		case CURVE_BEZIER:
			setCurve(timeline, frameIndex, input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());
			break;
		}
	}

	void setCurve (CurveTimeline timeline, int frameIndex, float cx1, float cy1, float cx2, float cy2) {
		timeline.setCurve(frameIndex, cx1, cy1, cx2, cy2);
	}
	
	/** Taken from LibGDX.
	 * Extends {@link DataInputStream} with additional convenience methods.
	 * @author Nathan Sweet */
	public static class DataInput extends DataInputStream {
		private char[] chars = new char[32];

		public DataInput (InputStream in) {
			super(in);
		}

		/** Reads a 1-5 byte int. */
		public int readInt (boolean optimizePositive) throws IOException {
			int b = read();
			int result = b & 0x7F;
			if ((b & 0x80) != 0) {
				b = read();
				result |= (b & 0x7F) << 7;
				if ((b & 0x80) != 0) {
					b = read();
					result |= (b & 0x7F) << 14;
					if ((b & 0x80) != 0) {
						b = read();
						result |= (b & 0x7F) << 21;
						if ((b & 0x80) != 0) {
							b = read();
							result |= (b & 0x7F) << 28;
						}
					}
				}
			}
			return optimizePositive ? result : ((result >>> 1) ^ -(result & 1));
		}

		/** Reads the length and string of UTF8 characters, or null.
		 * @return May be null. */
		public String readString () throws IOException {
			int charCount = readInt(true);
			switch (charCount) {
			case 0:
				return null;
			case 1:
				return "";
			}
			charCount--;
			if (chars.length < charCount) chars = new char[charCount];
			char[] chars = this.chars;
			// Try to read 7 bit ASCII chars.
			int charIndex = 0;
			int b = 0;
			while (charIndex < charCount) {
				b = read();
				if (b > 127) break;
				chars[charIndex++] = (char)b;
			}
			// If a char was not ASCII, finish with slow path.
			if (charIndex < charCount) readUtf8_slow(charCount, charIndex, b);
			return new String(chars, 0, charCount);
		}

		private void readUtf8_slow (int charCount, int charIndex, int b) throws IOException {
			char[] chars = this.chars;
			while (true) {
				switch (b >> 4) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					chars[charIndex] = (char)b;
					break;
				case 12:
				case 13:
					chars[charIndex] = (char)((b & 0x1F) << 6 | read() & 0x3F);
					break;
				case 14:
					chars[charIndex] = (char)((b & 0x0F) << 12 | (read() & 0x3F) << 6 | read() & 0x3F);
					break;
				}
				if (++charIndex >= charCount) break;
				b = read() & 0xFF;
			}
		}
	}

}
