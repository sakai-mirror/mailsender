package org.sakaiproject.mailsender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailsenderException extends Exception
{
	private static final long serialVersionUID = 1L;
	private List<Map<String, Object>> messages;

	public MailsenderException()
	{
	}

	public MailsenderException(List<Map<String, Object>> messages)
	{
		this.messages = messages;
	}

	public MailsenderException(String message, Exception cause)
	{
		super(message, cause);
	}

	public boolean hasMessages()
	{
		return (messages != null && messages.size() > 0);
	}

	public void addMessage(Map<String, Object> message)
	{
		if (messages == null)
		{
			messages = new ArrayList<Map<String, Object>>();
		}
		messages.add(message);
	}

	public void addMessage(String code, Object value)
	{
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put(code, value);
		addMessage(hm);
	}

	public List<Map<String, Object>> getMessages()
	{
		return messages;
	}
}
