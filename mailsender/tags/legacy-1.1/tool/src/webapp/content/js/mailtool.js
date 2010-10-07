var Mailtool = function()
{
	// attachment count keeps a net count of 
	var attachmentCount = 0;

	return {
		addAttachment : function(containerId)
		{
			// setup the screen to show differently if no attachments are showing
			if (attachmentCount == 0)
			{
				jQuery('#attachImgOuter').show();
				jQuery('#attachImgInner').hide();
				jQuery('#attachLink').hide();
				jQuery('#attachMoreLink').show();
			}
			Attachment.addAttachment(containerId);
			attachmentCount++;
		},

		removeAttachment : function(containerId, newDivId)
		{
			Attachment.removeAttachment(containerId, newDivId);
			attachmentCount--;
			if (attachmentCount == 0)
			{
				jQuery('#attachImgOuter').hide();
				jQuery('#attachImgInner').show();
				jQuery('#attachLink').show();
				jQuery('#attachMoreLink').hide();
			}
		}
	}; // end return
}(); // end namespace