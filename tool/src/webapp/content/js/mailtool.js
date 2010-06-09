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
				MailtoolUtil.showElement("attachImgOuter");
				MailtoolUtil.hideElement("attachImgInner");
				MailtoolUtil.hideElement("attachLink");
				MailtoolUtil.showElement("attachMoreLink");
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
				MailtoolUtil.hideElement("attachImgOuter");
				MailtoolUtil.showElement("attachImgInner");
				MailtoolUtil.showElement("attachLink");
				MailtoolUtil.hideElement("attachMoreLink");
			}
		}
	}; // end return
}(); // end namespace