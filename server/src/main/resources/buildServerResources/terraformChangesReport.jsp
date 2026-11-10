<%@ include file="/include-internal.jsp" %>
<jsp:useBean id="reportString" type="java.lang.String" scope="request"/>
<jsp:useBean id="reportCss" type="java.lang.String" scope="request"/>
<c:set var="reportDocument"><!DOCTYPE html><html><head><style>${reportCss}</style></head><body>${reportString}</body></html></c:set>
<iframe sandbox="allow-scripts" srcdoc="${fn:escapeXml(reportDocument)}" style="width: 100%; height: 80vh; border: none;"></iframe>