package net.wargearworld.bau.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.CombinedTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class FlattenedClipboardTransform {

	private final Clipboard original;
	private final Transform transform;

	/**
	 * Create a new instance.
	 *
	 * @param original  the original clipboard
	 * @param transform the transform
	 */
	public FlattenedClipboardTransform(Clipboard original, Transform transform) {
		checkNotNull(original);
		checkNotNull(transform);
		this.original = original;
		this.transform = transform;
	}

	/**
	 * Get the transformed region.
	 *
	 * @return the transformed region
	 */
	public Region getTransformedRegion() {
		Region region = original.getRegion();
		Vector3 minimum = region.getMinimumPoint().toVector3();
		Vector3 maximum = region.getMaximumPoint().toVector3();

		Transform transformAround = new CombinedTransform(
				new AffineTransform().translate(original.getOrigin().multiply(-1)), transform,
				new AffineTransform().translate(original.getOrigin()));

		Vector3[] corners = new Vector3[] { minimum, maximum, minimum.withX(maximum.getX()),
				minimum.withY(maximum.getY()), minimum.withZ(maximum.getZ()), maximum.withX(minimum.getX()),
				maximum.withY(minimum.getY()), maximum.withZ(minimum.getZ()) };

		for (int i = 0; i < corners.length; i++) {
			corners[i] = transformAround.apply(corners[i]);
		}

		Vector3 newMinimum = corners[0];
		Vector3 newMaximum = corners[0];// origin. corners[0]

		for (int i = 1; i < corners.length; i++) {
			newMinimum = newMinimum.getMinimum(corners[i]);
			newMaximum = newMaximum.getMaximum(corners[i]);
		}

		// After transformation, the points may not really sit on a block,
		// so we should expand the region for edge cases
		newMinimum = newMinimum.floor();
		newMaximum = newMaximum.ceil();

		return new CuboidRegion(newMinimum.toBlockPoint(), newMaximum.toBlockPoint());
	}

	/**
	 * Create an operation to copy from the original clipboard to the given extent.
	 *
	 * @param target the target
	 * @return the operation
	 */
	public Operation copyTo(Extent target) {
		BlockTransformExtent extent = new BlockTransformExtent(original, transform);
		ForwardExtentCopy copy = new ForwardExtentCopy(extent, original.getRegion(), original.getOrigin(), target,
				original.getOrigin());
		copy.setTransform(transform);
		if (original.hasBiomes()) {
			copy.setCopyingBiomes(true);
		}
		return copy;
	}

	public Clipboard getClip(Region rg) {
		Clipboard target;

		// If we have a transform, bake it into the copy
		if (transform.isIdentity()) {
			target = original;
		} else {
			target = new BlockArrayClipboard(rg);
			target.setOrigin(original.getOrigin());
			try {
				Operations.completeLegacy(copyTo(target));
			} catch (MaxChangedBlocksException e) {
				e.printStackTrace();
			}
		}
		return target;
	}

	/**
	 * Create a new instance to bake the transform with.
	 *
	 * @param original  the original clipboard
	 * @param transform the transform
	 * @param offset
	 * @return a builder
	 */
	public static FlattenedClipboardTransform transform(Clipboard original, Transform transform) {
		return new FlattenedClipboardTransform(original, transform);
	}

}