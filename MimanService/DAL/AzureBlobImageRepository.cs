
namespace DAL
{
    using DAL.Entity;
    using log4net;
    using Microsoft.WindowsAzure.Storage.Blob;
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Threading.Tasks;


    /// <summary>
    /// 
    /// </summary>
    public class AzureBlobImageRepository : IRepository<ComicImage, string>
    {
        private ILog logger = log4net.LogManager.GetLogger("default");
        private const string domainName = "";
        private const string containerName = "";
        private const string RWLSAS = "";
        private CloudBlobContainer blobContainer;


        public AzureBlobImageRepository()
        {
            string containerUri = string.Format("https://{0}.blob.core.windows.net/{1}{2}", domainName, containerName, RWLSAS);
            this.blobContainer = new CloudBlobContainer(new Uri(containerUri));
        }

        public async Task<bool> CreateAsync(ComicImage image)
        {
            bool IsSuccessful = true;
            CloudBlobDirectory blobDir = this.blobContainer.GetDirectoryReference(string.Join("/", image.Chapter.Book.BookId, image.Chapter.GetChapterName()));
            CloudBlockBlob blob = blobDir.GetBlockBlobReference(image.ImageFileName);
            image.ImageBlob.Position = 0;
            try
            {
                // string blockId = Convert.ToBase64String(Encoding.Unicode.GetBytes(image.ImageFileName));
                blob.Properties.ContentType = Util.GetMIMEFromFileExt(image.FileExt);
                blob.Properties.CacheControl = "public, max-age=7776000"; // 3 months
                await blob.UploadFromStreamAsync(image.ImageBlob);
                Console.WriteLine("Done with {0}", image.ImageFileName);
            }
            catch (Exception ex)
            {
                logger.ErrorFormat("Error when uploading {0} to {1}:{2}{3}", image.ImageFileName, blob.StorageUri.PrimaryUri, Environment.NewLine, ex.ToString());
                IsSuccessful = false;
            }
            return IsSuccessful;
        }

        public ComicImage GetAsync(string id)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// To fix the problem that some comics's chapters were saved into blob but not the metadata DB.
        /// </summary>
        /// <param name="books">Books in a certain category.</param>
        public void EnumerateChapters(IEnumerable<ComicBook> books)
        {
            var bookList = this.blobContainer.ListBlobs();
            foreach (var blobDir in bookList)
            {
                var chapters = new List<Chapter>();
                CloudBlobDirectory bookDir = blobDir as CloudBlobDirectory;
                int bookId;
                if (bookDir != null && int.TryParse(bookDir.Prefix.Trim('/'), out bookId))
                {
                    var book = books.FirstOrDefault(b => b.BookId == bookId);
                    if (book != null)
                    {
                        var chapterDirList = bookDir.ListBlobs().ToList();
                        foreach (var item in chapterDirList)
                        {
                            if (item as CloudBlobDirectory != null)
                            {
                                CloudBlobDirectory chapterDir = item as CloudBlobDirectory;
                                Chapter c = new Chapter();
                                int chapterIndex;
                                if (int.TryParse(GetChapterSegment(book.BookId.ToString(), chapterDir), out chapterIndex))
                                {
                                    c.Index = chapterIndex;
                                }
                                else
                                {
                                    c.DisplayName = GetChapterSegment(book.BookId.ToString(), chapterDir);
                                }
                                var imageBlobList = chapterDir.ListBlobs().ToList();
                                c.TotalPageNum = imageBlobList.Count;
                                chapters.Add(c);
                            }
                        }
                        book.Chapters.AddRange(chapters);
                        this.logger.DebugFormat("Done with {0}", book.BookId);
                    }
                }
            }
        }

        private static string GetChapterSegment(string bookId, CloudBlobDirectory chapterDir)
        {
            return chapterDir.Prefix.Replace(bookId, string.Empty).Trim('/');
        }
    }
}
