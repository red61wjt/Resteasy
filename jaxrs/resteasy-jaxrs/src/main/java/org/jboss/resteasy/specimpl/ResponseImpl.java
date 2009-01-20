package org.jboss.resteasy.specimpl;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.util.HttpResponseCodes;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResponseImpl extends Response
{
   private Object entity;
   private int status = HttpResponseCodes.SC_OK;
   private Headers<Object> metadata = new Headers<Object>();
   private NewCookie[] newCookies = {};
   private Annotation[] annotations;
   private Type genericType;

   public ResponseImpl(Object entity, int status, Headers<Object> metadata, NewCookie[] newCookies)
   {
      this.entity = entity;
      this.status = status;
      this.metadata = metadata;
      if (newCookies != null) this.newCookies = newCookies;
   }

   public ResponseImpl()
   {
   }

   @Override
   public Object getEntity()
   {
      return entity;
   }

   @Override
   public int getStatus()
   {
      return status;
   }

   @Override
   public MultivaluedMap<String, Object> getMetadata()
   {
      return metadata;
   }

   public NewCookie[] getNewCookies()
   {
      return newCookies;
   }

   public void setEntity(Object entity)
   {
      this.entity = entity;
   }

   public void setStatus(int status)
   {
      this.status = status;
   }

   public void setMetadata(MultivaluedMap<String, Object> metadata)
   {
      this.metadata.clear();
      this.metadata.putAll(metadata);
   }

   public void setNewCookies(NewCookie[] newCookies)
   {
      this.newCookies = newCookies;
   }

   public Annotation[] getAnnotations()
   {
      return annotations;
   }

   public void setAnnotations(Annotation[] annotations)
   {
      this.annotations = annotations;
   }

   public Type getGenericType()
   {
      return genericType;
   }

   public void setGenericType(Type genericType)
   {
      this.genericType = genericType;
   }
}