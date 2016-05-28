using DAL.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;

namespace MimanService.Controllers
{
    public class CategoryController : ApiController
    {
        static IEnumerable<Category> categories = (IEnumerable<Category>)HttpRuntime.Cache[Util.Utils.CategoryCacheKey];

        [HttpGet]
        public IEnumerable<Category> Categories()
        {
            return categories;
        }
    }
}