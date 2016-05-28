
namespace DAL
{
    using DAL.Entity;
    using System;
    using System.Text;


    /// <summary>
    /// 
    /// </summary>
    public static class Util
    {
        public static string ToBase64(string str)
        {
            return Convert.ToBase64String(Encoding.Unicode.GetBytes(str));
        }
        public static string FromBase64(string str)
        {
            return Encoding.Unicode.GetString(Convert.FromBase64String(str));
        }

        public static string GetMIMEFromFileExt(string ext)
        {
            if (ext.Equals(".jpg", StringComparison.InvariantCultureIgnoreCase))
            {
                return "image/jpeg";
            }
            else if (ext.Equals(".png", StringComparison.InvariantCultureIgnoreCase))
            {
                return "image/png";
            }
            else
            {
                return "text/plain";
            }
        }

        public static string GetChapterName(this Chapter chapter)
        {
            return string.IsNullOrEmpty(chapter.DisplayName) ?
                (chapter.IsVolume ? "卷" + chapter.Index.ToString() : chapter.Index.ToString())
                : chapter.DisplayName;
        }
    }
}
