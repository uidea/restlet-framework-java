/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Factory;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.data.Reference;

import com.noelios.restlet.impl.HttpServer;

/**
 * Servlet acting like an HTTP server connector. See the getTarget() method for details on how 
 * to provide a target for your server.<br/> Here is a sample configuration for your Restlet webapp:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;
 * &lt;!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/dtd/web-app_2_3.dtd"&gt;
 * &lt;web-app&gt;
 * 	&lt;display-name&gt;Server Servlet&lt;/display-name&gt;
 * 	&lt;description&gt;Servlet acting as a Restlet server connector&lt;/description&gt;
 * 
 * 	&lt;!-- Parameter indicating the target Restlet that will handle the call --&gt;
 * 	&lt;context-param&gt;
 * 		&lt;param-name&gt;org.restlet.target.class&lt;/param-name&gt;
 * 		&lt;param-value&gt;com.noelios.restlet.test.TraceTarget&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * 
 * 	&lt;!-- Parameter indicating the Servlet attribute to use to store the target Restlet reference --&gt;
 * 	&lt;context-param&gt;
 * 		&lt;param-name&gt;org.restlet.target.attribute&lt;/param-name&gt;
 * 		&lt;param-value&gt;org.restlet.target&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * 
 * 	&lt;!-- Parameter indicating the name of an initialization parameter that should be set with the ServerServlet context path --&gt;
 * 	&lt;context-param&gt;
 * 		&lt;param-name&gt;org.restlet.target.init.contextPath&lt;/param-name&gt;
 * 		&lt;param-value&gt;contextPath&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * 
 * 	&lt;!-- Definition of the ServerServlet class or a subclass --&gt;
 * 	&lt;servlet&gt;
 * 		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 * 		&lt;servlet-class&gt;com.noelios.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 * 	&lt;/servlet&gt;
 * 
 * 	&lt;!-- Mapping of requests to the ServerServlet --&gt;
 * 	&lt;servlet-mapping&gt;
 * 		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 * 		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * 	&lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;}
 * </pre>
 * Note that using this configuration, you can dynamically retrieve a context path from within your 
 * Restlet application that will have no knowledge of your Servlet integration details. This is the purpose
 * of the "contextPath" initialization argument. Its value will be dynamically provided by the ServerServlet
 * using this format:<br/>
 * <pre>
 * 	ContextPath = http://hostname:hostPort + ServletContextPath + ServletPath
 * 	ServletContextPath = result of call to servletRequest.getContextPath()
 * 	ServletPath = result of call to servletRequest.getServletPath()
 * </pre>
 * From within your RestletContainer subclass, you can retrieve this context path using this code:
 * <pre>
 * 	String contextPath = getParameters().get("contextPath")
 * </pre>
 * Just replace "contextPath" with the parameter name configured in your web.xml file under the 
 * "org.restlet.target.init.contextPath" Servlet context parameter. Also note that this init parameter
 * is only set after the constructor of your target class returns. This means that you should move any 
 * code that depends on the "contextPath" to the start() method. Now that you have your Restlet context path 
 * ready, you can use it to attach your root Restlet (or Router or Filter) to your target router or 
 * RestletContainer. You will typically do something like this in you constructor:
 * <pre>
 * 	attach(contextPath, myRootRestlet);
 * </pre>
 * Starting from "myRootRestlet", the coding is strictly similar to Restlet applications using the standalone 
 * mode.<br/><br/>
 * Finally, the enumeration of initParameters of your Servlet will be copied to the "parameters" property of 
 * your target component, or the closest owner component. This way, you can pass additional initialization 
 * parameters to your Restlet application, and share them with existing Servlets.
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServerServlet extends HttpServlet
{
   /** 
    * The Servlet context initialization parameter's name containing the target's 
    * class name to use to create the target instance. 
    */
   public static final String NAME_TARGET_CLASS = "org.restlet.target.class";
   
   /** 
    * The Servlet context initialization parameter's name containing the name of the 
    * Servlet context attribute that should be used to store the target instance. 
    */ 
   public static final String NAME_TARGET_ATTRIBUTE = "org.restlet.target.attribute";
   
   /** 
    * The Servlet context initialization parameter's name containing the name of the 
    * target initialization parameter to use to store the context path. This context path
    * is composed of the following parts: scheme, host name, [host port], webapp path,
    * servlet path. If this initialization parameter is not set in the Servlet context 
    * or config, then the setting is simply skipped. 
    */ 
   public static final String NAME_TARGET_INIT_CONTEXTPATH = "org.restlet.target.init.contextPath";

   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The target Restlet for Jetty calls. */
   protected Restlet target;

   /**
    * Constructor.
    */
   public ServerServlet()
   {
      this.target = null;
   }
   
   /**
    * Services a HTTP Servlet request as an uniform call.
    * @param request The HTTP Servlet request.
    * @param response The HTTP Servlet response.
    */
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
   	if(getTarget(request) != null)
      {
         HttpServer.handle(new ServletCall(request, response, getServletContext()), getTarget());
      }
   }

   /**
    * Returns the target Restlet handling calls.<br/>
    * For the first invocation, we look for an existing target in the application context, using the NAME_TARGET_ATTRIBUTE parameter.<br/>
    * We lookup for the attribute name in the servlet configuration, then in the application context.<br/>
    * If no target exists, we try to instantiate one based on the class name set in the NAME_TARGET_CLASS parameter.<br/>
    * We lookup for the class name in the servlet configuration, then in the application context.<br/>
    * Once the target is found, we wrap the servlet request and response into a uniform call and ask the target to handle it.<br/>
    * When the handling is done, we write the result back into the result object and return from the service method.
    * @param request The HTTP Servlet request.
    * @return The target Restlet handling calls.
    */
   public Restlet getTarget(HttpServletRequest request)
   {
   	Restlet result = this.target;
   	
      if(result == null)
      {
         synchronized(ServerServlet.class)
         {
            // Find the attribute name to use to store the target reference
            String targetAttributeName = findInitParameter(NAME_TARGET_ATTRIBUTE);

            if(targetAttributeName != null)
            {
               // Look up the attribute for a target
               result = (Restlet)getServletContext().getAttribute(targetAttributeName);

               if(result == null)
               {
                  // Try to instantiate a new target
                  // First, find the target class name
                  String targetClassName = findInitParameter(NAME_TARGET_CLASS);

                  if(targetClassName != null)
                  {
                     try
                     {
                        // Load the target class using the given class name
                        Class targetClass = Class.forName(targetClassName);

                        // Create a new instance of the target class
                        // and store it for reuse by other ServerServlets.
                        result = (Restlet)targetClass.newInstance();
                        getServletContext().setAttribute(NAME_TARGET_ATTRIBUTE, result);
                        
                        // Set if a context path needs to be transmitted
                     	String initContextPathName = findInitParameter(NAME_TARGET_INIT_CONTEXTPATH);
                     	if(initContextPathName != null)
                     	{
                        	// First, let's locate the closest component
                        	Component component = null;
                        	if(result instanceof Component)
                        	{
                        		// The target is probably a RestletContainer or RestletServer
                        		component = (Component)result;
                        	}
                        	else
                        	{
                        		// The target is probably a standalone Restlet or Filter or Router
                        		// Try to get its parent, even if chances to find one are low
                        		component = result.getOwner();
                        	}
                        	
                        	// Provide the context path as an init parameter
                        	if(component != null)
                        	{
                        		String scheme = request.getScheme();
                        		String hostName = request.getServerName();
                        		int hostPort = request.getServerPort();
                        		String servletPath = request.getContextPath() + request.getServletPath();
                        		String contextPath = Reference.toUri(scheme, hostName, hostPort, servletPath, null, null);
                        		component.getParameters().add(initContextPathName, contextPath);
                        		log("[Noelios Restlet Engine] - This context path has been provided to the target's init parameter \"" + initContextPathName + "\": " + contextPath);
                        		
                        		// Replace the default context client (if any)
                        		// by a special ServletContextClient instance 
                        		component.getClients().remove(Factory.CONTEXT_CLIENT_NAME);
                        		component.addClient(Factory.CONTEXT_CLIENT_NAME, new ServletContextClient(component, null, getServletContext()));
                        		log("[Noelios Restlet Engine] - The special ServletContextClient has been set on the target component under this name: " + Factory.CONTEXT_CLIENT_NAME);
                        		
                        		// Copy all initParameters in the component's parameters map
                        		String name;
                        		for(Enumeration names = getServletContext().getInitParameterNames(); names.hasMoreElements(); )
                        		{
                        			name = (String)names.nextElement();
                        			component.getParameters().add(name, getServletContext().getInitParameter(name));
                        		}
                        	}
                     	}
                     	
                     	// Starts the target Restlet
                     	result.start();
                     }
                     catch(ClassNotFoundException e)
                     {
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't find the class. Please check that your classpath includes " + targetClassName, e);
                     }
                     catch(InstantiationException e)
                     {
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the class. Please check this class has an empty constructor " + targetClassName, e);
                     }
                     catch(IllegalAccessException e)
                     {
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the class. Please check that you have to proper access rights to " + targetClassName, e);
                     }
							catch (Exception e)
							{
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't start the target Restlet.", e);
							}
                  }
                  else
                  {
                     log("[Noelios Restlet Engine] - The ServerServlet couldn't find the class name of the target Restlet. Please set the initialization parameter called " + NAME_TARGET_CLASS);
                  }
               }
            }
            else
            {
               log("[Noelios Restlet Engine] - The ServerServlet couldn't find the attribute name of the target Restlet. Please set the initialization parameter called " + NAME_TARGET_ATTRIBUTE);
            }

            this.target = result;
         }
      }
      
      return result;
   }

   /**
    * Returns the target Restlet handling calls.
    * @return The target Restlet handling calls.
    */
   public Restlet getTarget()
   {
      return this.target;
   }

   /**
    * Sets the target Restlet handling calls.
    * @param target The target Restlet handling calls.
    */
   public void setTarget(Restlet target)
   {
      this.target = target;
   }

   /**
    * Finds an initialization parameter by first checking the Servlet context.
    * If nothing is found, then it checks the Servlet config.
    * @param name The parameter's name.
    * @return The parameter's value or null.
    */
   protected String findInitParameter(String name)
   {
      String result = getServletContext().getInitParameter(name);
      if(result == null)
      {
      	result = getServletConfig().getInitParameter(name);
      }

      return result;
   }
   
}
