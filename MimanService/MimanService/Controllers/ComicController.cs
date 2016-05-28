namespace MimanService.Controllers
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Net;
    using System.Net.Http;
    using System.Web.Http;
    using DAL.Entity;
    using System.Web;
    using MimanService.Util;
    using System.Web.Mvc;
    using MimanService.Models;

    public class ComicController : ApiController
    {
        private static readonly int maxBookResultSize = 50;
        private static readonly string searchResultCacheItemPrefix = "Keyword:";

        [System.Web.Http.HttpGet]
        public IEnumerable<BookListItem> GetBooksByCategory(int Id, int startIndex = 0, int resultSize = 10)
        {
            resultSize = Math.Min(maxBookResultSize, resultSize);
            var books = (IEnumerable<BookListItem>)HttpRuntime.Cache[Id.ToString()];
            if (books != null)
            {
                return books.Skip(startIndex).Take(resultSize);
            }
            return null;
        }

        [System.Web.Http.HttpGet]
        public BookDetailItem GetBookDetailById(int id)
        {
            var allBookDetailItemDict = HttpRuntime.Cache[Utils.AllBookDetailItemDictCacheKey] as Dictionary<int, BookDetailItem>;
            BookDetailItem book;
            allBookDetailItemDict.TryGetValue(id, out book);
            return book;
        }

        [System.Web.Http.HttpGet]
        public IEnumerable<BookListItem> GetBooksById([FromUri] int[] ids)
        {
            var booksDict = HttpRuntime.Cache[Utils.AllBookListItemDictCacheKey] as Dictionary<int, BookListItem>;
            List<BookListItem> books = null;
            if (ids != null && ids.Length > 0)
            {
                books = new List<BookListItem>();
                foreach (var id in ids)
                {
                    if (booksDict.ContainsKey(id))
                    {
                        books.Add(booksDict[id]);
                    }
                }
            }

            return books;
        }

        [System.Web.Http.HttpGet]
        public IEnumerable<BookListItem> Search(string keywords, int startIndex = 0, int resultSize = 10)
        {
            string[] keywordList = keywords.Split(new string[] { " " }, StringSplitOptions.RemoveEmptyEntries);
            RemoveExtraSpaces(ref keywordList);
            resultSize = Math.Min(maxBookResultSize, resultSize);
            string cacheItemKey = searchResultCacheItemPrefix + string.Join(";", keywordList);
            var result = HttpRuntime.Cache[cacheItemKey] as List<BookListItem>;
            var detailedItems = new List<BookDetailItem>();
            if (result == null)
            {
                var allBooks = HttpRuntime.Cache[Utils.AllBookDetailItemsCacheKey] as List<BookDetailItem>;
                detailedItems.AddRange(allBooks.Where(book =>
                    book.Author.ContainsIgnoreCase(keywordList)
                        || book.Title.ContainsIgnoreCase(keywordList)));
                detailedItems.AddRange(allBooks.Where(book =>
                    book.Intro.ContainsIgnoreCase(keywordList) && !detailedItems.Contains(book)));

                if (detailedItems.Count > 0)
                {
                    var allBookListItemDict = HttpRuntime.Cache[Utils.AllBookListItemDictCacheKey] as Dictionary<int, BookListItem>;
                    result = detailedItems.Select(r => allBookListItemDict[r.Id]).ToList();
                    Utils.Add2Cache(cacheItemKey, result, new TimeSpan(1, 0, 0));
                }
            }

            if (result != null && result.Count > 0)
            {
                startIndex = startIndex > result.Count ? 0 : startIndex;
                resultSize = Math.Min(result.Count - startIndex, resultSize);
                return result.Skip(startIndex).Take(resultSize);
            }
            else
            {
                return null;
            }

        }

        private void RemoveExtraSpaces(ref string[] keywordList)
        {
            for (int i = 0; i < keywordList.Length; i++)
            {
                keywordList[i] = keywordList[i].Trim();
            }
        }
    }
}
