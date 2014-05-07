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

import com.esotericsoftware.spine.attachments.Attachment;

public class Slot {
	final SlotData data;
	final Bone bone;
	private final Skeleton skeleton;
	float r, g, b, a;

	Attachment attachment;
	private float attachmentTime;
	private float[] attachmentVertices = new float[0];
	int attachmentVerticesCount;

	Slot() {
		data = null;
		bone = null;
		skeleton = null;
		r = 1f;
		g = 1f;
		b = 1f;
		a = 1f;
	}

	public Slot(SlotData data, Skeleton skeleton, Bone bone) {
		if (data == null)
			throw new IllegalArgumentException("data cannot be null.");
		if (skeleton == null)
			throw new IllegalArgumentException("skeleton cannot be null.");
		if (bone == null)
			throw new IllegalArgumentException("bone cannot be null.");
		this.data = data;
		this.skeleton = skeleton;
		this.bone = bone;
		setToSetupPose();
	}

	/** Copy constructor. */
	public Slot(Slot slot, Skeleton skeleton, Bone bone) {
		if (slot == null)
			throw new IllegalArgumentException("slot cannot be null.");
		if (skeleton == null)
			throw new IllegalArgumentException("skeleton cannot be null.");
		if (bone == null)
			throw new IllegalArgumentException("bone cannot be null.");
		data = slot.data;
		this.skeleton = skeleton;
		this.bone = bone;
		r = slot.r;
		g = slot.g;
		b = slot.b;
		a = slot.a;
		attachment = slot.attachment;
		attachmentTime = slot.attachmentTime;
	}

	public SlotData getData() {
		return data;
	}

	public Skeleton getSkeleton() {
		return skeleton;
	}

	public Bone getBone() {
		return bone;
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

	/** @return May be null. */
	public Attachment getAttachment() {
		return attachment;
	}

	/**
	 * Sets the attachment, resets {@link #getAttachmentTime()}, and clears {@link #getAttachmentVertices()}.
	 * 
	 * @param attachment
	 *            May be null.
	 */
	public void setAttachment(Attachment attachment) {
		if (this.attachment == attachment)
			return;
		this.attachment = attachment;
		attachmentTime = skeleton.time;
		attachmentVerticesCount = 0;
	}

	public void setAttachmentTime(float time) {
		attachmentTime = skeleton.time - time;
	}

	/** Returns the time since the attachment was set. */
	public float getAttachmentTime() {
		return skeleton.time - attachmentTime;
	}

	public void setAttachmentVertices(float[] attachmentVertices) {
		this.attachmentVertices = attachmentVertices;
	}

	public float[] getAttachmentVertices() {
		return attachmentVertices;
	}

	public int getAttachmentVerticesCount() {
		return attachmentVerticesCount;
	}

	void setToSetupPose(int slotIndex) {
		r = data.r;
		g = data.g;
		b = data.b;
		a = data.a;
		setAttachment(data.attachmentName == null ? null : skeleton.getAttachment(slotIndex, data.attachmentName));
	}

	public void setToSetupPose() {
		setToSetupPose(skeleton.data.slots.indexOf(data));
	}

	public String toString() {
		return data.name;
	}
}
