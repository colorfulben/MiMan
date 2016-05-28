
namespace DAL.Entity
{
    using System.IO;


    /// <summary>
    /// 
    /// </summary>
    public class ComicImage
    {
        private string imageFileName;
        public Chapter Chapter { get; set; }
        public string ImageIndex { get; set; }
        public Stream ImageBlob { get; set; }
        public string FileExt { get; set; }

        public string ImageFileName
        {
            get
            {
                if (string.IsNullOrEmpty(this.imageFileName))
                {
                    this.imageFileName = this.ImageIndex + FileExt;
                }
                return this.imageFileName;
            }
        }
    }
}
