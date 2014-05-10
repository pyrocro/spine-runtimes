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

import playn.core.GroupLayer;
import playn.core.PlayN;

import com.esotericsoftware.spine.attachments.Attachment;

public class Skeleton {

	final SkeletonData data;
	final ArrayList<Bone> bones;
	final ArrayList<Slot> slots;
	ArrayList<Slot> drawOrder;
	Skin skin;
	float time;
	float r = 1f, g = 1f, b = 1f, a = 1f;

	private GroupLayer playncoordinates;
	private GroupLayer spinecoordinates;
	GroupLayer root;

	public Skeleton(SkeletonData data) {
		if (data == null)
			throw new IllegalArgumentException("data cannot be null.");
		this.data = data;
		createLayers();

		bones = new ArrayList<Bone>(data.bones.size());
		for (BoneData boneData : data.bones) {
			Bone parent = boneData.parent == null ? null : bones.get(data.bones.indexOf(boneData.parent));
			Bone bone = new Bone(boneData, parent);
			bones.add(bone);
		}

		slots = new ArrayList<Slot>(data.slots.size());
		drawOrder = new ArrayList<Slot>(data.slots.size());
		for (SlotData slotData : data.slots) {
			Bone bone = bones.get(data.bones.indexOf(slotData.boneData));
			Slot slot = new Slot(slotData, this, bone);
			slots.add(slot);
			drawOrder.add(slot);
		}
		Utils.updateDrawOrder(this);
	}

	/** Copy constructor. */
	public Skeleton(Skeleton skeleton) {
		if (skeleton == null)
			throw new IllegalArgumentException("skeleton cannot be null.");
		data = skeleton.data;
		createLayers();

		bones = new ArrayList<Bone>(skeleton.bones.size());
		for (Bone bone : skeleton.bones) {
			Bone parent = bone.parent == null ? null : bones.get(skeleton.bones.indexOf(bone.parent));
			Bone nbone = new Bone(bone, parent);
			bones.add(nbone);
		}

		slots = new ArrayList<Slot>(skeleton.slots.size());
		for (Slot slot : skeleton.slots) {
			Bone bone = bones.get(skeleton.bones.indexOf(slot.bone));
			Slot newSlot = new Slot(slot, this, bone);
			slots.add(newSlot);
		}

		drawOrder = new ArrayList<Slot>(slots.size());
		for (Slot slot : skeleton.drawOrder)
			drawOrder.add(slots.get(skeleton.slots.indexOf(slot)));
		Utils.updateDrawOrder(this);

		skin = skeleton.skin;
		time = skeleton.time;
	}

	private void createLayers() {

		// A Layer using PlayN coordinates for managing the skeleton.
		playncoordinates = PlayN.graphics().createGroupLayer();

		// A specific Layer for transforming PlayN coordinates into Spine coordinates
		spinecoordinates = PlayN.graphics().createGroupLayer();
		// spinecoordinates.setTranslation(0f, PlayN.graphics().height());
		spinecoordinates.setScaleY(-1f);
		playncoordinates.add(spinecoordinates);

		// Parent layer of the skeleton.
		root = PlayN.graphics().createGroupLayer();
		spinecoordinates.add(root);
	}

	/** Sets the bones and slots to their setup pose values. */
	public void setToSetupPose() {
		setBonesToSetupPose();
		setSlotsToSetupPose();
	}

	public void setBonesToSetupPose() {
		ArrayList<Bone> bones = this.bones;
		int bsize = bones.size();
		for (int i = 0, n = bsize; i < n; i++)
			bones.get(i).setToSetupPose();
	}

	public void setSlotsToSetupPose() {
		ArrayList<Slot> slots = this.slots;
		int ssize = slots.size();
		for (int i = 0; i < ssize; i++)
			drawOrder.set(i, slots.get(i));
		for (int i = 0, n = ssize; i < n; i++)
			slots.get(i).setToSetupPose(i);
		Utils.updateDrawOrder(this);
	}

	public SkeletonData getData() {
		return data;
	}

	public ArrayList<Bone> getBones() {
		return bones;
	}

	/** @return May return null. */
	public Bone getRootBone() {
		if (bones.size() == 0)
			return null;
		return bones.get(0);
	}

	/** @return May be null. */
	public Bone findBone(String boneName) {
		if (boneName == null)
			throw new IllegalArgumentException("boneName cannot be null.");
		ArrayList<Bone> bones = this.bones;
		int bsize = bones.size();
		for (int i = 0, n = bsize; i < n; i++) {
			Bone bone = bones.get(i);
			if (bone.data.name.equals(boneName))
				return bone;
		}
		return null;
	}

	/** @return -1 if the bone was not found. */
	public int findBoneIndex(String boneName) {
		if (boneName == null)
			throw new IllegalArgumentException("boneName cannot be null.");
		ArrayList<Bone> bones = this.bones;
		int bsize = bones.size();
		for (int i = 0, n = bsize; i < n; i++)
			if (bones.get(i).data.name.equals(boneName))
				return i;
		return -1;
	}

	public ArrayList<Slot> getSlots() {
		return slots;
	}

	/** @return May be null. */
	public Slot findSlot(String slotName) {
		if (slotName == null)
			throw new IllegalArgumentException("slotName cannot be null.");
		ArrayList<Slot> slots = this.slots;
		int ssize = slots.size();
		for (int i = 0, n = ssize; i < n; i++) {
			Slot slot = slots.get(i);
			if (slot.data.name.equals(slotName))
				return slot;
		}
		return null;
	}

	/** @return -1 if the bone was not found. */
	public int findSlotIndex(String slotName) {
		if (slotName == null)
			throw new IllegalArgumentException("slotName cannot be null.");
		ArrayList<Slot> slots = this.slots;
		int ssize = slots.size();
		for (int i = 0, n = ssize; i < n; i++)
			if (slots.get(i).data.name.equals(slotName))
				return i;
		return -1;
	}

	/** Returns the slots in the order they will be drawn. The returned array may be modified to change the draw order. */
	public ArrayList<Slot> getDrawOrder() {
		return drawOrder;
	}

	/** Sets the slots and the order they will be drawn. */
	public void setDrawOrder(ArrayList<Slot> drawOrder) {
		this.drawOrder = drawOrder;
		Utils.updateDrawOrder(this);
	}

	/** @return May be null. */
	public Skin getSkin() {
		return skin;
	}

	/**
	 * Sets a skin by name.
	 * 
	 * @see #setSkin(Skin)
	 */
	public void setSkin(String skinName) {
		Skin skin = data.findSkin(skinName);
		if (skin == null)
			throw new IllegalArgumentException("Skin not found: " + skinName);
		setSkin(skin);
	}

	/**
	 * Sets the skin used to look up attachments not found in the {@link SkeletonData#getDefaultSkin() default skin}. Attachments from the new skin
	 * are attached if the corresponding attachment from the old skin was attached. If there was no old skin, each slot's setup mode attachment is
	 * attached from the new skin.
	 * 
	 * @param newSkin
	 *            May be null.
	 */
	public void setSkin(Skin newSkin) {
		if (skin == null) {
			ArrayList<Slot> slots = this.slots;
			int ssize = slots.size();
			for (int i = 0, n = ssize; i < n; i++) {
				Slot slot = slots.get(i);
				String name = slot.data.attachmentName;
				if (name != null) {
					Attachment attachment = newSkin.getAttachment(i, name);
					if (attachment != null)
						slot.setAttachment(attachment);
				}
			}
		} else if (newSkin != null) //
			newSkin.attachAll(this, skin);
		skin = newSkin;
	}

	/** @return May be null. */
	public Attachment getAttachment(String slotName, String attachmentName) {
		return getAttachment(data.findSlotIndex(slotName), attachmentName);
	}

	/** @return May be null. */
	public Attachment getAttachment(int slotIndex, String attachmentName) {
		if (attachmentName == null)
			throw new IllegalArgumentException("attachmentName cannot be null.");
		if (skin != null) {
			Attachment attachment = skin.getAttachment(slotIndex, attachmentName);
			if (attachment != null)
				return attachment;
		}
		if (data.defaultSkin != null)
			return data.defaultSkin.getAttachment(slotIndex, attachmentName);
		return null;
	}

	/**
	 * @param attachmentName
	 *            May be null.
	 */
	public void setAttachment(String slotName, String attachmentName) {
		if (slotName == null)
			throw new IllegalArgumentException("slotName cannot be null.");
		ArrayList<Slot> slots = this.slots;
		int ssize = slots.size();
		for (int i = 0, n = ssize; i < n; i++) {
			Slot slot = slots.get(i);
			if (slot.data.name.equals(slotName)) {
				Attachment attachment = null;
				if (attachmentName != null) {
					attachment = getAttachment(i, attachmentName);
					if (attachment == null)
						throw new IllegalArgumentException("Attachment not found: " + attachmentName + ", for slot: " + slotName);
				}
				slot.setAttachment(attachment);
				return;
			}
		}
		throw new IllegalArgumentException("Slot not found: " + slotName);
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public float getG() {
		return g;
	}

	public void setG(float g) {
		this.g = g;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}

	public float getA() {
		return a;
	}

	public void setA(float a) {
		this.a = a;
	}

	public boolean getFlipX() {
		return playncoordinates.scaleX() == -1f;
	}

	public void setFlipX(boolean flipX) {
		this.playncoordinates.setScaleX(flipX ? -1f : 1f);
	}

	public boolean getFlipY() {
		return playncoordinates.scaleY() == -1f;
	}

	public void setFlipY(boolean flipY) {
		this.playncoordinates.setScaleY(flipY ? -1f : 1f);
	}

	public float getX() {
		return playncoordinates.tx();
	}

	public void setX(float x) {
		this.playncoordinates.setTx(x);
	}

	public float getY() {
		return playncoordinates.ty();
	}

	public void setY(float y) {
		playncoordinates.setTy(y);
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public GroupLayer rootLayer() {
		return playncoordinates;
	}

	public void update(float delta) {
		time += delta;
	}

	public String toString() {
		return data.name != null ? data.name : super.toString();
	}

}
