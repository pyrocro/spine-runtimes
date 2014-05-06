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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.spine.Animation.AttachmentTimeline;
import com.esotericsoftware.spine.Animation.ColorTimeline;
import com.esotericsoftware.spine.Animation.CurveTimeline;
import com.esotericsoftware.spine.Animation.FfdTimeline;
import com.esotericsoftware.spine.Animation.RotateTimeline;
import com.esotericsoftware.spine.Animation.Timeline;
import com.esotericsoftware.spine.Animation.TranslateTimeline;
import com.esotericsoftware.spine.Animation.ScaleTimeline;
import com.esotericsoftware.spine.Animation.DrawOrderTimeline;
import com.esotericsoftware.spine.Animation.EventTimeline;
import com.esotericsoftware.spine.attachments.AtlasAttachmentLoader;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.AttachmentLoader;
import com.esotericsoftware.spine.attachments.AttachmentType;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.esotericsoftware.spine.attachments.SkinnedMeshAttachment;

@SuppressWarnings("all")
public class SkeletonJson {
	private AttachmentLoader attachmentLoader;
	float scale;

	public SkeletonJson(Atlas atlas) {
		this(new AtlasAttachmentLoader(atlas));
	}

	public SkeletonJson(AttachmentLoader attachmentLoader) {
		if (attachmentLoader == null) throw new IllegalArgumentException("attachmentLoader cannot be null.");
		this.attachmentLoader = attachmentLoader;
		scale = 1;
	}
	
	public SkeletonData readSkeletonData(String name, Map<String, Object> root) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		if (root == null) throw new IllegalArgumentException("root cannot be null.");

		SkeletonData skeletonData = new SkeletonData();
		skeletonData.name = name;

		// Bones.
		for (Map<String, Object> boneMap : (List<Map<String, Object>>) root.get("bones")) {
			BoneData parent = null;
			if (boneMap.containsKey("parent")) {
				parent = skeletonData.findBone((String) boneMap.get("parent"));
				if (parent == null)
					throw new RuntimeException("Parent bone not found: " + boneMap.get("parent"));
			}
			BoneData boneData = new BoneData((String) boneMap.get("name"), parent);
			boneData.length = getFloat(boneMap, "length", 0) * scale;
			boneData.x = getFloat(boneMap, "x", 0) * scale;
			boneData.y = getFloat(boneMap, "y", 0) * scale;
			boneData.rotation = getFloat(boneMap, "rotation", 0);
			boneData.scaleX = getFloat(boneMap, "scaleX", 1);
			boneData.scaleY = getFloat(boneMap, "scaleY", 1);
			boneData.inheritScale = getBoolean(boneMap, "inheritScale", true);
			boneData.inheritRotation = getBoolean(boneMap, "inheritRotation", true);
			skeletonData.addBone(boneData);
		}

		// Slots.
		if (root.containsKey("slots")) {
			for (Map<String, Object> slotMap : (List<Map<String, Object>>) root.get("slots")) {
				String slotName = (String) slotMap.get("name");
				String boneName = (String) slotMap.get("bone");
				BoneData boneData = skeletonData.findBone(boneName);
				if (boneData == null)
					throw new RuntimeException("Slot bone not found: " + boneName);
				SlotData slotData = new SlotData(slotName, boneData);

				if (slotMap.containsKey("color")) {
					String color = (String) slotMap.get("color");
					slotData.r = toColor(color, 0);
					slotData.g = toColor(color, 1);
					slotData.b = toColor(color, 2);
					slotData.a = toColor(color, 3);
				}

				if (slotMap.containsKey("attachment"))
					slotData.attachmentName = (String) slotMap.get("attachment");

				if (slotMap.containsKey("additive"))
					slotData.additiveBlending = (Boolean) slotMap.get("additive");

				skeletonData.addSlot(slotData);
			}
		}

		// Skins.
		if (root.containsKey("skins")) {
			for (Map.Entry<String,Object> entry : ((Map<String,Object>) root.get("skins")).entrySet()) {
				Skin skin = new Skin(entry.getKey());
				for (Map.Entry<String,Object> slotEntry : ((Map<String,Object>) entry.getValue()).entrySet()) {
					int slotIndex = skeletonData.findSlotIndex(slotEntry.getKey());
					for (Map.Entry<String,Object> attachmentEntry : ((Map<String,Object>) slotEntry.getValue()).entrySet()) {
						Attachment attachment = readAttachment(skin, attachmentEntry.getKey(), (Map<String, Object>) attachmentEntry.getValue());
						if (attachment != null) skin.addAttachment(slotIndex, attachmentEntry.getKey(), attachment);
					}
				}
				skeletonData.addSkin(skin);
				if (skin.name.equals("default"))
					skeletonData.defaultSkin = skin;
			}
		}

		// Events.
		if (root.containsKey("events")) {
			for (Map.Entry<String,Object> entry : ((Map<String,Object>) root.get("events")).entrySet()) {
				Map<String, Object> entryMap = (Map<String, Object>) entry.getValue();
				EventData eventData = new EventData(entry.getKey());
				eventData.setInt(getInt(entryMap, "int", 0));
				eventData.setFloat(getFloat(entryMap, "float", 0));
				eventData.setString(getString(entryMap, "string", null));
				skeletonData.addEvent(eventData);
			}
		}

		// Animations.
		if (root.containsKey("animations")) {
			for (Map.Entry<String,Object> entry : ((Map<String,Object>) root.get("animations")).entrySet())
				readAnimation(entry.getKey(), (Map<String, Object>) entry.getValue(), skeletonData);
		}

		skeletonData.bones.trimToSize();
		skeletonData.slots.trimToSize();
		skeletonData.skins.trimToSize();
		skeletonData.animations.trimToSize();
		return skeletonData;
	}

	private Attachment readAttachment(Skin skin, String name, Map<String, Object> map) {
		if (map.containsKey("name"))
			name = (String) map.get("name");

		AttachmentType type = AttachmentType.region;
		if (map.containsKey("type"))
			type = AttachmentType.valueOf((String) map.get("type"));

		String path = name;
		if (map.containsKey("path"))
			path = (String) map.get("path");

		switch (type) {
		case region:
			RegionAttachment region = attachmentLoader.newRegionAttachment(skin, name, path);
			if (region == null) return null;
			region.setPath(path);
			region.setX(getFloat(map, "x", 0) * scale);
			region.setY(getFloat(map, "y", 0) * scale);
			region.setScaleX(getFloat(map, "scaleX", 1));
			region.setScaleY(getFloat(map, "scaleY", 1));
			region.setRotation(getFloat(map, "rotation", 0));
			region.setWidth(getFloat(map, "width", 32) * scale);
			region.setHeight(getFloat(map, "height", 32) * scale);
			region.updateOffset();

			if (map.containsKey("color")) {
				String color = (String) map.get("color");
				region.setR(toColor(color, 0));
				region.setG(toColor(color, 1));
				region.setB(toColor(color, 2));
				region.setA(toColor(color, 3));
			}

			return region;
		case mesh: {
				MeshAttachment mesh = attachmentLoader.newMeshAttachment(skin, name, path);
				if (mesh == null) return null;

				mesh.setPath(path); 
				mesh.setVertices(getFloatArray(map, "vertices", scale));
				mesh.setTriangles(getShortArray(map, "triangles"));
				mesh.setRegionUVs(getFloatArray(map, "uvs", 1));
				mesh.updateUVs();

				if (map.containsKey("color")) {
					String color = (String) map.get("color");
					mesh.setR(toColor(color, 0));
					mesh.setG(toColor(color, 1));
					mesh.setB(toColor(color, 2));
					mesh.setA(toColor(color, 3));
				}

				mesh.setHullLength(getInt(map, "hull", 0) * 2);
				if (map.containsKey("edges")) mesh.setEdges(getIntArray(map, "edges"));
				mesh.setWidth(getInt(map, "width", 0) * scale);
				mesh.setHeight(getInt(map, "height", 0) * scale);

				return mesh;
			}
		case skinnedmesh: {
				SkinnedMeshAttachment mesh = attachmentLoader.newSkinnedMeshAttachment(skin, name, path);
				if (mesh == null) return null;

				mesh.setPath(path);
				float[] uvs = getFloatArray(map, "uvs", 1);
				float[] vertices = getFloatArray(map, "vertices", 1);
				ArrayList<Float> weights = new ArrayList<Float>(uvs.length * 3 * 3);
				ArrayList<Integer> bones = new ArrayList<Integer>(uvs.length * 3);
				float scale = this.scale;
				for (int i = 0, n = vertices.length; i < n; ) {
					int boneCount = (int) vertices[i++];
					bones.add(boneCount);
					for (int nn = i + boneCount * 4; i < nn; ) {
						bones.add((int)vertices[i]);
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
					String color = (String) map.get("color");
					mesh.setR(toColor(color, 0));
					mesh.setG(toColor(color, 1));
					mesh.setB(toColor(color, 2));
					mesh.setA(toColor(color, 3));
				}

				mesh.setHullLength(getInt(map, "hull", 0) * 2);
				if (map.containsKey("edges")) mesh.setEdges(getIntArray(map, "edges"));
				mesh.setWidth(getInt(map, "width", 0) * scale);
				mesh.setHeight(getInt(map, "height", 0) * scale);

				return mesh;
			}
		case boundingbox:
			BoundingBoxAttachment box = attachmentLoader.newBoundingBoxAttachment(skin, name);
			if (box == null) return null;
			box.setVertices(getFloatArray(map, "vertices", scale));
			return box;
		}
		return null;
	}
	
	private float[] getFloatArray(Map<String, Object> map, String name, float scale) {
		List<Number> list = (List<Number>) map.get(name);
		int fsize = list.size();
		float[] values = new float[fsize];
		if (scale == 1f) {
			for (int i = 0; i < fsize; i++)
				values[i] = list.get(i).floatValue();
		} else {
			for (int i = 0; i < fsize; i++)
				values[i] = list.get(i).floatValue() * scale;
		}
		return values;
	}

	private short[] getShortArray(Map<String, Object> map, String name) {
		List<Number> list = (List<Number>) map.get(name);
		int ssize = list.size();
		short[] values = new short[ssize];
		for (int i = 0; i < ssize; i++)
			values[i] = list.get(i).shortValue();
		return values;
	}

	private int[] getIntArray(Map<String, Object> map, String name) {
		List<Number> list = (List<Number>) map.get(name);
		int isize = list.size();
		int[] values = new int[isize];
		for (int i = 0; i < isize; i++)
			values[i] = list.get(i).intValue();
		return values;
	}

	private float getFloat(Map<String, Object> map, String name, float defaultValue) {
		if (!map.containsKey(name))
			return defaultValue;
		return ((Number) map.get(name)).floatValue();
	}
	
	private int getInt(Map<String, Object> map, String name, int defaultValue) {
		if (!map.containsKey(name))
			return defaultValue;
		return ((Number) map.get(name)).intValue();
	}

	private boolean getBoolean(Map<String, Object> map, String name, boolean defaultValue) {
		if (!map.containsKey(name))
			return defaultValue;
		return (Boolean) map.get(name);
	}

	private String getString(Map<String, Object> map, String name, String defaultValue) {
		if (!map.containsKey(name))
			return defaultValue;
		return (String) map.get(name);
	}

	public static float toColor(String hexString, int colorIndex) {
		if (hexString.length() != 8)
			throw new IllegalArgumentException("Color hexidecimal length must be 8, recieved: " + hexString);
		return Integer.parseInt(hexString.substring(colorIndex * 2, colorIndex * 2 + 2), 16) / 255f;
	}
	
	private void readAnimation(String name, Map<String, Object> map, SkeletonData skeletonData) {
		ArrayList<Timeline> timelines = new ArrayList<Timeline>();
		float duration = 0;
		float scale = this.scale;

		if (map.containsKey("slots")) {
			for (Map.Entry<String,Object> entry : ((Map<String,Object>) map.get("slots")).entrySet()) {
				String slotName = entry.getKey();
				int slotIndex = skeletonData.findSlotIndex(slotName);
				Map<String, Object> timelineMap = (Map<String, Object>) entry.getValue();

				for (Map.Entry<String,Object> timelineEntry : timelineMap.entrySet()) {
					List<Map<String, Object>> values = (List<Map<String, Object>>) timelineEntry.getValue();
					String timelineName = (String) timelineEntry.getKey();
					if (timelineName.equals("color")) {
						ColorTimeline timeline = new ColorTimeline(values.size());
						timeline.slotIndex = slotIndex;

						int frameIndex = 0;
						for (Map<String, Object> valueMap : values) {
							float time = ((Number) valueMap.get("time")).floatValue();
							String c = (String) valueMap.get("color");
							timeline.setFrame(frameIndex, time, toColor(c, 0), toColor(c, 1), toColor(c, 2), toColor(c, 3));
							readCurve(timeline, frameIndex, valueMap);
							frameIndex++;
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() * 5 - 5]);

					} else if (timelineName.equals("attachment")) {
						AttachmentTimeline timeline = new AttachmentTimeline(values.size());
						timeline.slotIndex = slotIndex;

						int frameIndex = 0;
						for (Map<String, Object> valueMap : values) {
							float time = ((Number) valueMap.get("time")).floatValue();
							timeline.setFrame(frameIndex++, time, (String) valueMap.get("name"));
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);

					} else
						throw new RuntimeException("Invalid timeline type for a slot: " + timelineName + " (" + slotName + ")");
				}
			}
		}

		if (map.containsKey("bones")) {
			for (Map.Entry<String,Object> entry : ((Map<String,Object>) map.get("bones")).entrySet()) {
				String boneName = entry.getKey();
				int boneIndex = skeletonData.findBoneIndex(boneName);
				if (boneIndex == -1)
					throw new RuntimeException("Bone not found: " + boneName);

				Map<String, Object> timelineMap = (Map<String, Object>) entry.getValue();
				for (Map.Entry<String, Object> timelineEntry : timelineMap.entrySet()) {
					List<Map<String, Object>> values = (List<Map<String, Object>>) timelineEntry.getValue();
					String timelineName = (String) timelineEntry.getKey();
					if (timelineName.equals("rotate")) {
						RotateTimeline timeline = new RotateTimeline(values.size());
						timeline.boneIndex = boneIndex;

						int frameIndex = 0;
						for (Map<String, Object> valueMap : values) {
							float time = ((Number) valueMap.get("time")).floatValue();
							timeline.setFrame(frameIndex, time, ((Number) valueMap.get("angle")).floatValue());
							readCurve(timeline, frameIndex, valueMap);
							frameIndex++;
						}
						timelines.add(timeline);
						duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() * 2 - 2]);

					} else if (timelineName.equals("translate") || timelineName.equals("scale")) {
						TranslateTimeline timeline;
						float timelineScale = 1;
						if (timelineName.equals("scale"))
							timeline = new ScaleTimeline(values.size());
						else {
							timeline = new TranslateTimeline(values.size());
							timelineScale = scale;
						}
						timeline.boneIndex = boneIndex;

						int frameIndex = 0;
						for (Map<String, Object> valueMap : values) {
							float time = ((Number) valueMap.get("time")).floatValue();
							float x = valueMap.containsKey("x") ? ((Number) valueMap.get("x")).floatValue() : 0f;
							float y = valueMap.containsKey("y") ? ((Number) valueMap.get("y")).floatValue() : 0f;
							timeline.setFrame(frameIndex, time, (float)x * timelineScale, (float)y * timelineScale);
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
			for (Map.Entry<String,Object> ffdMap : ((Map<String,Object>) map.get("ffd")).entrySet()) {
				Skin skin = skeletonData.findSkin(ffdMap.getKey());
				for (Map.Entry<String, Object> slotMap : ((Map<String, Object>) ffdMap.getValue()).entrySet()) {
					int slotIndex = skeletonData.findSlotIndex(slotMap.getKey());
					for (Map.Entry<String, Object> meshMap : ((Map<String, Object>) slotMap.getValue()).entrySet()) {
						List<Map<String, Object>> values = (List<Map<String, Object>>) meshMap.getValue();
						FfdTimeline timeline = new FfdTimeline(values.size());
						Attachment attachment = skin.getAttachment(slotIndex, meshMap.getKey());
						if (attachment == null) throw new RuntimeException("FFD attachment not found: " + meshMap.getKey());
						timeline.slotIndex = slotIndex;
						timeline.attachment = attachment;

						int vertexCount;
						if (attachment instanceof MeshAttachment)
							vertexCount = ((MeshAttachment)attachment).getVertices().length;
						else
							vertexCount = ((SkinnedMeshAttachment)attachment).getWeights().length / 3 * 2;

						int frameIndex = 0;
						for (Map<String, Object> valueMap : values) {
							float[] vertices;
							if (!valueMap.containsKey("vertices")) {
								if (attachment instanceof MeshAttachment)
									vertices = ((MeshAttachment)attachment).getVertices();
								else
									vertices = new float[vertexCount];
							} else {
								List<Number> verticesValue = (List<Number>) valueMap.get("vertices");
								vertices = new float[vertexCount];
								int start = getInt(valueMap, "offset", 0);
								int vvsize = verticesValue.size();
								if (scale == 1) {
									for (int i = 0; i < vvsize; i++)
										vertices[i + start] = verticesValue.get(i).floatValue();
								} else {
									for (int i = 0; i < vvsize; i++)
										vertices[i + start] = verticesValue.get(i).floatValue() * scale;
								}
								if (attachment instanceof MeshAttachment) {
									float[] meshVertices = ((MeshAttachment)attachment).getVertices();
									for (int i = 0, n = vertices.length; i < n; i++)
										vertices[i] += meshVertices[i];
								}
							}

							timeline.setFrame(frameIndex, ((Number) valueMap.get("time")).floatValue(), vertices);
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
			List<Map<String, Object>> values = (List<Map<String, Object>>) map.get("draworder");
			DrawOrderTimeline timeline = new DrawOrderTimeline(values.size());
			int slotCount = skeletonData.slots.size();
			int frameIndex = 0;
			for (Map<String, Object> drawOrderMap : values) {
				int[] drawOrder = null;
				if (drawOrderMap.containsKey("offsets")) {
					drawOrder = new int[slotCount];
					for (int i = slotCount - 1; i >= 0; i--)
						drawOrder[i] = -1;
					List<Map<String, Object>> offsets = (List<Map<String, Object>>) drawOrderMap.get("offsets");
					int[] unchanged = new int[slotCount - offsets.size()];
					int originalIndex = 0, unchangedIndex = 0;
					for (Map<String, Object> offsetMap : offsets) {
						int slotIndex = skeletonData.findSlotIndex((String) offsetMap.get("slot"));
						if (slotIndex == -1) throw new RuntimeException("Slot not found: " + offsetMap.get("slot"));
						// Collect unchanged items.
						while (originalIndex != slotIndex)
							unchanged[unchangedIndex++] = originalIndex++;
						// Set changed items.
						drawOrder[originalIndex + ((Number) offsetMap.get("offset")).intValue()] = originalIndex++;
					}
					// Collect remaining unchanged items.
					while (originalIndex < slotCount)
						unchanged[unchangedIndex++] = originalIndex++;
					// Fill in unchanged items.
					for (int i = slotCount - 1; i >= 0; i--)
						if (drawOrder[i] == -1) drawOrder[i] = unchanged[--unchangedIndex];
				}
				timeline.setFrame(frameIndex++, ((Number) drawOrderMap.get("time")).floatValue(), drawOrder);
			}
			timelines.add(timeline);
			duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);
		}

		if (map.containsKey("events")) {
			List<Map<String, Object>> eventsMap = (List<Map<String, Object>>) map.get("events");
			EventTimeline timeline = new EventTimeline(eventsMap.size());
			int frameIndex = 0;
			for (Map<String, Object> eventMap : eventsMap) {
				EventData eventData = skeletonData.findEvent((String) eventMap.get("name"));
				if (eventData == null) throw new RuntimeException("Event not found: " + eventMap.get("name"));
				Event e = new Event(eventData);
				e.setInt(getInt(eventMap, "int", eventData.getInt()));
				e.setFloat(getFloat(eventMap, "float", eventData.getFloat()));
				e.setString(getString(eventMap, "string", eventData.getString()));
				timeline.setFrame(frameIndex++, ((Number) eventMap.get("time")).floatValue(), e);
			}
			timelines.add(timeline);
			duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);
		}

		timelines.trimToSize();
		skeletonData.addAnimation(new Animation(name, timelines, duration));
	}

	private void readCurve(CurveTimeline timeline, int frameIndex, Map<String, Object> valueMap) {
		if (!valueMap.containsKey("curve"))
			return;
		Object curveObject = valueMap.get("curve");
		if (curveObject.equals("stepped"))
			timeline.setStepped(frameIndex);
		else if (curveObject instanceof List) {
			List<Number> curve = (List<Number>)curveObject;
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
