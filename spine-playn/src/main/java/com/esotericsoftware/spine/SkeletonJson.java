/******************************************************************************
 * Spine Runtimes Software License
 * Version 2.1
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software (typically granted by licensing Spine), you
 * may not (a) modify, translate, adapt or otherwise create derivative works,
 * improvements of the Software or develop new applications using the Software
 * or (b) remove, delete, alter or obscure any trademarks or any copyright,
 * trademark, patent or other intellectual property or proprietary rights
 * notices on or in the Software, including any copy thereof. Redistributions
 * in binary or source form must include this license and terms.
 * 
 * THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine;

import java.util.ArrayList;

import playn.core.Color;
import playn.core.Json;

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

public class SkeletonJson {
	private AttachmentLoader attachmentLoader;
	float scale;

	public SkeletonJson(Atlas atlas) {
		this(new AtlasAttachmentLoader(atlas));
	}

	public SkeletonJson(AttachmentLoader attachmentLoader) {
		if (attachmentLoader == null)
			throw new IllegalArgumentException("attachmentLoader cannot be null.");
		this.attachmentLoader = attachmentLoader;
		scale = 1;
	}

	public SkeletonData readSkeletonData(String name, Json.Object root) {
		if (name == null)
			throw new IllegalArgumentException("name cannot be null.");
		if (root == null)
			throw new IllegalArgumentException("root cannot be null.");

		SkeletonData skeletonData = new SkeletonData();
		skeletonData.name = name;

		// Bones.
		for (Json.Object boneMap : root.getArray("bones", Json.Object.class)) {
			BoneData parent = null;
			if (boneMap.containsKey("parent")) {
				parent = skeletonData.findBone(boneMap.getString("parent"));
				if (parent == null)
					throw new RuntimeException("Parent bone not found: " + boneMap.getString("parent"));
			}
			BoneData boneData = new BoneData(boneMap.getString("name"), parent);
			boneData.length = boneMap.getNumber("length", 0) * scale;
			boneData.x = boneMap.getNumber("x", 0) * scale;
			boneData.y = boneMap.getNumber("y", 0) * scale;
			boneData.rotation = boneMap.getNumber("rotation", 0);
			boneData.scaleX = boneMap.getNumber("scaleX", 1);
			boneData.scaleY = boneMap.getNumber("scaleY", 1);
			boneData.inheritScale = boneMap.getBoolean("inheritScale", true);
			boneData.inheritRotation = boneMap.getBoolean("inheritRotation", true);
			skeletonData.addBone(boneData);
		}

		// Slots.
		if (root.containsKey("slots")) {
			for (Json.Object slotMap : root.getArray("slots", Json.Object.class)) {
				String slotName = slotMap.getString("name");
				String boneName = slotMap.getString("bone");
				BoneData boneData = skeletonData.findBone(boneName);
				if (boneData == null)
					throw new RuntimeException("Slot bone not found: " + boneName);
				SlotData slotData = new SlotData(slotName, boneData);

				if (slotMap.containsKey("color")) {
					String color = slotMap.getString("color");
					slotData.r = toColor(color, 0);
					slotData.g = toColor(color, 1);
					slotData.b = toColor(color, 2);
					slotData.a = toColor(color, 3);
				}

				if (slotMap.containsKey("attachment"))
					slotData.attachmentName = slotMap.getString("attachment");

				if (slotMap.containsKey("additive"))
					slotData.additiveBlending = slotMap.getBoolean("additive");

				skeletonData.addSlot(slotData);
			}
		}

		// Skins.
		if (root.containsKey("skins")) {
			for (String entryKey : root.getObject("skins").keys()) {
				Json.Object entryValue = root.getObject("skins").getObject(entryKey);
				Skin skin = new Skin(entryKey);
				for (String slotEntryKey : entryValue.keys()) {
					Json.Object slotEntryValue = entryValue.getObject(slotEntryKey);
					int slotIndex = skeletonData.findSlotIndex(slotEntryKey);
					for (String attachmentEntryKey : slotEntryValue.keys()) {
						Json.Object attachmentEntryValue = slotEntryValue.getObject(attachmentEntryKey);
						Attachment attachment = readAttachment(skin, attachmentEntryKey, attachmentEntryValue);
						if (attachment != null)
							skin.addAttachment(slotIndex, attachmentEntryKey, attachment);
					}
				}
				skeletonData.addSkin(skin);
				if (skin.name.equals("default"))
					skeletonData.defaultSkin = skin;
			}
		}

		// Events.
		if (root.containsKey("events")) {
			for (String entryKey : root.getObject("events").keys()) {
				Json.Object entryValue = root.getObject("events").getObject(entryKey);
				EventData eventData = new EventData(entryKey);
				eventData.setInt(entryValue.getInt("int", 0));
				eventData.setFloat(entryValue.getNumber("float", 0));
				eventData.setString(entryValue.getString("string", null));
				skeletonData.addEvent(eventData);
			}
		}

		// Animations.
		if (root.containsKey("animations")) {
			for (String entryKey : root.getObject("animations").keys()) {
				Json.Object entryValue = root.getObject("animations").getObject(entryKey);
				readAnimation(entryKey, entryValue, skeletonData);
			}
		}

		skeletonData.bones.trimToSize();
		skeletonData.slots.trimToSize();
		skeletonData.skins.trimToSize();
		skeletonData.animations.trimToSize();
		return skeletonData;
	}

	private Attachment readAttachment(Skin skin, String name, Json.Object map) {
		if (map.containsKey("name"))
			name = map.getString("name");

		AttachmentType type = AttachmentType.region;
		if (map.containsKey("type"))
			type = AttachmentType.valueOf(map.getString("type"));

		String path = name;
		if (map.containsKey("path"))
			path = map.getString("path");

		switch (type) {
		case region:
			RegionAttachment region = attachmentLoader.newRegionAttachment(skin, name, path);
			if (region == null)
				return null;
			region.setPath(path);
			region.setX(map.getNumber("x", 0) * scale);
			region.setY(map.getNumber("y", 0) * scale);
			region.setScaleX(map.getNumber("scaleX", 1));
			region.setScaleY(map.getNumber("scaleY", 1));
			region.setRotation(map.getNumber("rotation", 0));
			region.setWidth(map.getNumber("width", 32) * scale);
			region.setHeight(map.getNumber("height", 32) * scale);
			// FIXME region.updateOffset();

			if (map.containsKey("color")) {
				String color = map.getString("color");
				region.setR(toColor(color, 0));
				region.setG(toColor(color, 1));
				region.setB(toColor(color, 2));
				region.setA(toColor(color, 3));
			}

			return region;
		case mesh: {
			MeshAttachment mesh = attachmentLoader.newMeshAttachment(skin, name, path);
			if (mesh == null)
				return null;

			mesh.setPath(path);
			mesh.setVertices(getFloatArray(map, "vertices", scale));
			mesh.setTriangles(getShortArray(map, "triangles"));
			mesh.setRegionUVs(getFloatArray(map, "uvs", 1));
			mesh.updateUVs();

			if (map.containsKey("color")) {
				String color = map.getString("color");
				mesh.setR(toColor(color, 0));
				mesh.setG(toColor(color, 1));
				mesh.setB(toColor(color, 2));
				mesh.setA(toColor(color, 3));
			}

			mesh.setHullLength(map.getInt("hull", 0) * 2);
			if (map.containsKey("edges"))
				mesh.setEdges(getIntArray(map, "edges"));
			mesh.setWidth(map.getInt("width", 0) * scale);
			mesh.setHeight(map.getInt("height", 0) * scale);

			return mesh;
		}
		case skinnedmesh: {
			SkinnedMeshAttachment mesh = attachmentLoader.newSkinnedMeshAttachment(skin, name, path);
			if (mesh == null)
				return null;

			mesh.setPath(path);
			float[] uvs = getFloatArray(map, "uvs", 1);
			float[] vertices = getFloatArray(map, "vertices", 1);
			ArrayList<Float> weights = new ArrayList<Float>(uvs.length * 3 * 3);
			ArrayList<Integer> bones = new ArrayList<Integer>(uvs.length * 3);
			float scale = this.scale;
			for (int i = 0, n = vertices.length; i < n;) {
				int boneCount = (int) vertices[i++];
				bones.add(boneCount);
				for (int nn = i + boneCount * 4; i < nn;) {
					bones.add((int) vertices[i]);
					weights.add(vertices[i + 1] * scale);
					weights.add(vertices[i + 2] * scale);
					weights.add(vertices[i + 3]);
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
			mesh.setTriangles(getShortArray(map, "triangles"));
			mesh.setRegionUVs(uvs);
			mesh.updateUVs();

			if (map.containsKey("color")) {
				String color = map.getString("color");
				mesh.setR(toColor(color, 0));
				mesh.setG(toColor(color, 1));
				mesh.setB(toColor(color, 2));
				mesh.setA(toColor(color, 3));
			}

			mesh.setHullLength(map.getInt("hull", 0) * 2);
			if (map.containsKey("edges"))
				mesh.setEdges(getIntArray(map, "edges"));
			mesh.setWidth(map.getInt("width", 0) * scale);
			mesh.setHeight(map.getInt("height", 0) * scale);

			return mesh;
		}
		case boundingbox:
			BoundingBoxAttachment box = attachmentLoader.newBoundingBoxAttachment(skin, name);
			if (box == null)
				return null;
			box.setVertices(getFloatArray(map, "vertices", scale));
			return box;
		}
		return null;
	}

	private float[] getFloatArray(Json.Object map, String name, float scale) {
		Json.TypedArray<Float> array = map.getArray(name, Float.class);
		int fsize = array.length();
		float[] values = new float[fsize];
		if (scale == 1f) {
			for (int i = 0; i < fsize; i++)
				values[i] = array.get(i);
		} else {
			for (int i = 0; i < fsize; i++)
				values[i] = array.get(i) * scale;
		}
		return values;
	}

	private short[] getShortArray(Json.Object map, String name) {
		Json.TypedArray<Integer> array = map.getArray(name, Integer.class);
		int ssize = array.length();
		short[] values = new short[ssize];
		for (int i = 0; i < ssize; i++)
			values[i] = array.get(i).shortValue();
		return values;
	}

	private int[] getIntArray(Json.Object map, String name) {
		Json.TypedArray<Integer> array = map.getArray(name, Integer.class);
		int ssize = array.length();
		int[] values = new int[ssize];
		for (int i = 0; i < ssize; i++)
			values[i] = array.get(i);
		return values;
	}

	public static float toColor(String hexString, int colorIndex) {
		if (hexString.length() != 8)
			throw new IllegalArgumentException("Color hexidecimal length must be 8, recieved: " + hexString);
		return Integer.parseInt(hexString.substring(colorIndex * 2, colorIndex * 2 + 2), 16) / 255f;
	}

	public static int toColor(String hexString) {
		if (hexString.length() != 8)
			throw new IllegalArgumentException("Color hexidecimal length must be 8, recieved: " + hexString);
		return Color.argb(Integer.parseInt(hexString.substring(6, 8), 16), Integer.parseInt(hexString.substring(0, 2), 16),
				Integer.parseInt(hexString.substring(2, 4), 16), Integer.parseInt(hexString.substring(4, 6), 16));
	}

	private void readAnimation(String name, Json.Object map, SkeletonData skeletonData) {
		ArrayList<Timeline> timelines = new ArrayList<Timeline>();
		float duration = 0;
		float scale = this.scale;

		if (map.containsKey("slots")) {
			for (String entryKey : map.getObject("slots").keys()) {
				Json.Object entryValue = map.getObject("slots").getObject(entryKey);
				String slotName = entryKey;
				int slotIndex = skeletonData.findSlotIndex(slotName);

				for (String timelineEntryKey : entryValue.keys()) {
					Json.TypedArray<Json.Object> values = entryValue.getArray(timelineEntryKey, Json.Object.class);
					String timelineName = (String) timelineEntryKey;
					if (timelineName.equals("color")) {
						ColorTimeline timeline = new ColorTimeline(values.length());
						timeline.slotIndex = slotIndex;

						int frameIndex = 0;
						for (Json.Object valueMap : values) {
							float time = valueMap.getNumber("time");
							String color = valueMap.getString("color");
							timeline.setFrame(frameIndex, time, toColor(color, 0), toColor(color, 1), toColor(color, 2), toColor(color, 3));
							readCurve(timeline, frameIndex, valueMap);
							frameIndex++;
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() * 5 - 5]);

					} else if (timelineName.equals("attachment")) {
						AttachmentTimeline timeline = new AttachmentTimeline(values.length());
						timeline.slotIndex = slotIndex;

						int frameIndex = 0;
						for (Json.Object valueMap : values) {
							float time = valueMap.getNumber("time");
							timeline.setFrame(frameIndex++, time, valueMap.getString("name"));
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);

					} else
						throw new RuntimeException("Invalid timeline type for a slot: " + timelineName + " (" + slotName + ")");
				}
			}
		}

		if (map.containsKey("bones")) {
			for (String entryKey : map.getObject("bones").keys()) {
				Json.Object entryValue = map.getObject("bones").getObject(entryKey);
				String boneName = entryKey;
				int boneIndex = skeletonData.findBoneIndex(boneName);
				if (boneIndex == -1)
					throw new RuntimeException("Bone not found: " + boneName);

				for (String timelineEntryKey : entryValue.keys()) {
					Json.TypedArray<Json.Object> values = entryValue.getArray(timelineEntryKey, Json.Object.class);
					String timelineName = timelineEntryKey;
					if (timelineName.equals("rotate")) {
						RotateTimeline timeline = new RotateTimeline(values.length());
						timeline.boneIndex = boneIndex;

						int frameIndex = 0;
						for (Json.Object valueMap : values) {
							float time = valueMap.getNumber("time");
							timeline.setFrame(frameIndex, time, valueMap.getNumber("angle"));
							readCurve(timeline, frameIndex, valueMap);
							frameIndex++;
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() * 2 - 2]);

					} else if (timelineName.equals("translate") || timelineName.equals("scale")) {
						TranslateTimeline timeline;
						float timelineScale = 1;
						if (timelineName.equals("scale"))
							timeline = new ScaleTimeline(values.length());
						else {
							timeline = new TranslateTimeline(values.length());
							timelineScale = scale;
						}
						timeline.boneIndex = boneIndex;

						int frameIndex = 0;
						for (Json.Object valueMap : values) {
							float time = valueMap.getNumber("time");
							float x = valueMap.containsKey("x") ? valueMap.getNumber("x") : 0f;
							float y = valueMap.containsKey("y") ? valueMap.getNumber("y") : 0f;
							timeline.setFrame(frameIndex, time, (float) x * timelineScale, (float) y * timelineScale);
							readCurve(timeline, frameIndex, valueMap);
							frameIndex++;
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() * 3 - 3]);

					} else
						throw new RuntimeException("Invalid timeline type for a bone: " + timelineName + " (" + boneName + ")");
				}
			}
		}

		if (map.containsKey("ffd")) {
			for (String entryKey : map.getObject("ffd").keys()) {
				Json.Object entryValue = map.getObject("ffd").getObject(entryKey);
				Skin skin = skeletonData.findSkin(entryKey);
				for (String slotEntryKey : entryValue.keys()) {
					Json.Object slotEntryValue = entryValue.getObject(slotEntryKey);
					int slotIndex = skeletonData.findSlotIndex(slotEntryKey);
					for (String meshEntryKey : slotEntryValue.keys()) {
						Json.TypedArray<Json.Object> values = slotEntryValue.getArray(meshEntryKey, Json.Object.class);
						FfdTimeline timeline = new FfdTimeline(values.length());
						Attachment attachment = skin.getAttachment(slotIndex, meshEntryKey);
						if (attachment == null)
							throw new RuntimeException("FFD attachment not found: " + meshEntryKey);
						timeline.slotIndex = slotIndex;
						timeline.attachment = attachment;

						int vertexCount;
						if (attachment instanceof MeshAttachment)
							vertexCount = ((MeshAttachment) attachment).getVertices().length;
						else
							vertexCount = ((SkinnedMeshAttachment) attachment).getWeights().length / 3 * 2;

						int frameIndex = 0;
						for (Json.Object valueMap : values) {
							float[] vertices;
							if (!valueMap.containsKey("vertices")) {
								if (attachment instanceof MeshAttachment)
									vertices = ((MeshAttachment) attachment).getVertices();
								else
									vertices = new float[vertexCount];
							} else {
								Json.TypedArray<Float> verticesValue = valueMap.getArray("vertices", Float.class);
								vertices = new float[vertexCount];
								int start = valueMap.getInt("offset", 0);
								int vvsize = verticesValue.length();
								if (scale == 1) {
									for (int i = 0; i < vvsize; i++)
										vertices[i + start] = verticesValue.get(i).floatValue();
								} else {
									for (int i = 0; i < vvsize; i++)
										vertices[i + start] = verticesValue.get(i).floatValue() * scale;
								}
								if (attachment instanceof MeshAttachment) {
									float[] meshVertices = ((MeshAttachment) attachment).getVertices();
									for (int i = 0, n = vertices.length; i < n; i++)
										vertices[i] += meshVertices[i];
								}
							}

							timeline.setFrame(frameIndex, valueMap.getNumber("time"), vertices);
							readCurve(timeline, frameIndex, valueMap);
							frameIndex++;
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);
					}
				}
			}
		}

		if (map.containsKey("draworder")) {
			Json.TypedArray<Json.Object> values = map.getArray("draworder", Json.Object.class);
			DrawOrderTimeline timeline = new DrawOrderTimeline(values.length());
			int slotCount = skeletonData.slots.size();
			int frameIndex = 0;
			for (Json.Object drawOrderMap : values) {
				int[] drawOrder = null;
				if (drawOrderMap.containsKey("offsets")) {
					drawOrder = new int[slotCount];
					for (int i = slotCount - 1; i >= 0; i--)
						drawOrder[i] = -1;

					Json.TypedArray<Json.Object> offsets = drawOrderMap.getArray("offsets", Json.Object.class);
					int[] unchanged = new int[slotCount - offsets.length()];
					int originalIndex = 0, unchangedIndex = 0;
					for (Json.Object offsetMap : offsets) {
						int slotIndex = skeletonData.findSlotIndex(offsetMap.getString("slot"));
						if (slotIndex == -1)
							throw new RuntimeException("Slot not found: " + offsetMap.getString("slot"));
						// Collect unchanged items.
						while (originalIndex != slotIndex)
							unchanged[unchangedIndex++] = originalIndex++;
						// Set changed items.
						drawOrder[originalIndex + offsetMap.getInt("offset")] = originalIndex++;
					}
					// Collect remaining unchanged items.
					while (originalIndex < slotCount)
						unchanged[unchangedIndex++] = originalIndex++;
					// Fill in unchanged items.
					for (int i = slotCount - 1; i >= 0; i--)
						if (drawOrder[i] == -1)
							drawOrder[i] = unchanged[--unchangedIndex];
				}
				timeline.setFrame(frameIndex++, drawOrderMap.getNumber("time"), drawOrder);
			}
			timelines.add(timeline);
			duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);
		}

		if (map.containsKey("events")) {
			Json.TypedArray<Json.Object> eventsMap = map.getArray("events", Json.Object.class);
			EventTimeline timeline = new EventTimeline(eventsMap.length());
			int frameIndex = 0;
			for (Json.Object eventMap : eventsMap) {
				EventData eventData = skeletonData.findEvent(eventMap.getString("name"));
				if (eventData == null)
					throw new RuntimeException("Event not found: " + eventMap.getString("name"));
				Event e = new Event(eventData);
				e.setInt(eventMap.getInt("int", eventData.getInt()));
				e.setFloat(eventMap.getNumber("float", eventData.getFloat()));
				e.setString(eventMap.getString("string", eventData.getString()));
				timeline.setFrame(frameIndex++, eventMap.getNumber("time"), e);
			}
			timelines.add(timeline);
			duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);
		}

		timelines.trimToSize();
		skeletonData.addAnimation(new Animation(name, timelines, duration));
	}

	private void readCurve(CurveTimeline timeline, int frameIndex, Json.Object valueMap) {
		if (!valueMap.containsKey("curve"))
			return;
		if (valueMap.isString("curve") && valueMap.getString("curve").equals("stepped"))
			timeline.setStepped(frameIndex);
		else if (valueMap.isArray("curve")) {
			Json.TypedArray<Float> curve = valueMap.getArray("curve", Float.class);
			timeline.setCurve(frameIndex, curve.get(0).floatValue(), curve.get(1).floatValue(), curve.get(2).floatValue(), curve.get(3).floatValue());
		}
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

}
