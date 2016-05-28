
namespace DAL
{
    using DAL.Entity;
    using log4net;
    using System;
    using System.Configuration;
    using System.IO;
    using System.Threading.Tasks;

    public class LocalImageRepository : IRepository<ComicImage, String>
    {
        private const int bufferSize = 16384; // 16K
        private ILog logger = log4net.LogManager.GetLogger("default");
        private string baseDir = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);


        public LocalImageRepository()
        {
            var workDir = ConfigurationManager.AppSettings["FSBaseDir"];
            if (!string.IsNullOrWhiteSpace(workDir))
            {
                this.baseDir = workDir;
            }
        }

        public LocalImageRepository(string workDir)
            : this()
        {
            if (!string.IsNullOrWhiteSpace(workDir))
            {
                this.baseDir = workDir;
                this.logger.DebugFormat("Base directory set to {0}", this.baseDir);
            }
        }

        public async Task<bool> CreateAsync(ComicImage image)
        {
            string fileName = Path.Combine(baseDir, image.Chapter.Book.BookId.ToString(), image.Chapter.GetChapterName(), image.ImageFileName);
            CreateDirIfNotExist(fileName);
            if (!File.Exists(fileName))
            {
                image.ImageBlob.Position = 0;
                using (var fs = File.Create(fileName))
                {
                    await image.ImageBlob.CopyToAsync(fs, bufferSize);
                }
            }
            return true;
        }
        public ComicImage GetAsync(string id)
        {
            throw new NotImplementedException();
        }

        private static void CreateDirIfNotExist(string fileName)
        {
            FileInfo fi = new FileInfo(fileName);
            if (!fi.Directory.Exists)
            {
                Directory.CreateDirectory(fi.DirectoryName);
            }
        }
    }
}
