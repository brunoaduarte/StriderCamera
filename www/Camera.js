
var argscheck = require('cordova/argscheck'),
exec = require('cordova/exec'),
Camera = require('./Camera'),
CameraPopoverHandle = require('./CameraPopoverHandle');

var cameraExport = {};

//Tack on the Camera Constants to the base camera plugin.
for (var key in Camera) {
	cameraExport[key] = Camera[key];
}

/**
 * Gets a picture from source defined by "options.sourceType", and returns the
 * image as defined by the "options.destinationType" option.

 * The defaults are sourceType=CAMERA and destinationType=FILE_URI.
 *
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
cameraExport.getPicture = function(successCallback, errorCallback, options) {
	argscheck.checkArgs('fFO', 'Camera.getPicture', arguments);
	options = options || {};
	var getValue = argscheck.getValue;

	var quality = getValue(options.quality, 50);
	var destinationType = getValue(options.destinationType, Camera.DestinationType.FILE_URI);
	var sourceType = getValue(options.sourceType, Camera.PictureSourceType.CAMERA);
	var targetWidth = getValue(options.targetWidth, -1);
	var targetHeight = getValue(options.targetHeight, -1);
	var encodingType = getValue(options.encodingType, Camera.EncodingType.JPEG);
	var mediaType = getValue(options.mediaType, Camera.MediaType.PICTURE);
	var allowEdit = !!options.allowEdit;
	var correctOrientation = !!options.correctOrientation;
	var saveToPhotoAlbum = !!options.saveToPhotoAlbum;
	var popoverOptions = getValue(options.popoverOptions, null);
	var cameraDirection = getValue(options.cameraDirection, Camera.Direction.BACK);

	var args = [quality, destinationType, sourceType, targetWidth, targetHeight, encodingType,
	            mediaType, allowEdit, correctOrientation, saveToPhotoAlbum, popoverOptions, cameraDirection];

	exec(successCallback, errorCallback, "Camera", "takePicture", args);
	return new CameraPopoverHandle();
};

cameraExport.cleanup = function(successCallback, errorCallback) {
	exec(successCallback, errorCallback, "Camera", "cleanup", []);
};

module.exports = cameraExport;
