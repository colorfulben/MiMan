using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using log4net;
using System.Web.Http.Filters;
using System.Net.Http;
using System.Net;
using System.IO;

namespace MimanService.Util
{
    public class CustomErrorHandlerAttribute : ExceptionFilterAttribute
    {
        static ILog logger = log4net.LogManager.GetLogger("default");

        public override void OnException(HttpActionExecutedContext context)
        {
            logger.ErrorFormat("Error when accessing: {0}{1}{2}", context.Request.RequestUri, Environment.NewLine, context.Exception.ToString());
            context.Response = new HttpResponseMessage(HttpStatusCode.InternalServerError);
        }
    }
}