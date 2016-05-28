using DAL;
using DAL.Entity;
using log4net;
using System;
using System.Web;
using System.Web.Caching;

namespace MimanService.Util
{
    public static class Utils
    {
        public static readonly string CategoryCacheKey = "Category";
        public static readonly string AllBookDetailItemsCacheKey = "AllBookDetailItems";
        public static readonly string AllBookDetailItemDictCacheKey = "AllBookDetailItemDict";
        public static readonly string AllBookListItemDictCacheKey = "AllBookListItemDict";

        static ILog logger = log4net.LogManager.GetLogger("default");
        public static void Add2Cache(object key, object value)
        {
            Add2Cache(key, value, new TimeSpan(180, 0, 0, 0, 0));
        }

        public static void Add2Cache(object key, object value, TimeSpan expiration)
        {
            HttpRuntime.Cache.Add(key.ToString(), value, null, Cache.NoAbsoluteExpiration, expiration, CacheItemPriority.High, Utils.CacheItemRemovedCallback);
        }

        public static bool ContainsIgnoreCase(this string str, string keyword)
        {
            return str.IndexOf(keyword, 0, StringComparison.InvariantCultureIgnoreCase) >= 0;
        }

        public static bool ContainsIgnoreCase(this string str, string[] keywords)
        {
            bool match = true;
            foreach (var keyword in keywords)
            {
                match = match && (str.IndexOf(keyword, 0, StringComparison.InvariantCultureIgnoreCase) >= 0);
            }
            return match;
        }

        private static void CacheItemRemovedCallback(string key, object value, CacheItemRemovedReason reason)
        {
            logger.WarnFormat("{0} got removed, reason: {1}", key, reason);
        }
    }
}