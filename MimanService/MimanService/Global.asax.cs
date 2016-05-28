using DAL;
using DAL.Entity;
using log4net;
using MimanService.Models;
using MimanService.Util;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;

namespace MimanService
{
    public class WebApiApplication : System.Web.HttpApplication
    {
        static ILog logger = log4net.LogManager.GetLogger("default");

        protected void Application_Start()
        {
            AreaRegistration.RegisterAllAreas();
            GlobalConfiguration.Configure(WebApiConfig.Register);
            FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
            RouteConfig.RegisterRoutes(RouteTable.Routes);
            BundleConfig.RegisterBundles(BundleTable.Bundles);
            log4net.Config.XmlConfigurator.Configure(new FileInfo(Server.MapPath("~/Web.config")));
            logger.InfoFormat("Application started.");
            var startDt = DateTime.Now;
            using (ComicBooksRepository repo = new ComicBooksRepository("name=SQLAzure"))
            {
                Utils.Add2Cache(Utils.CategoryCacheKey, repo.GetCategories());
                var allBookListItems = new List<BookListItem>();
                var allBookListItemDict = new Dictionary<int, BookListItem>();
                var allBookDetailItems = new List<BookDetailItem>();
                var allBookDetailItemDict = new Dictionary<int, BookDetailItem>();
                foreach (var category in repo.GetCategories()) //TODO:
                {
                    var books = repo.GetAvailableBooksByCategory(category);
                    var bookListItems = books.Select(b => new BookListItem()
                    {
                        Id = b.Id,
                        Title = b.Title,
                        Cover = b.Cover
                    });
                    allBookListItems.AddRange(bookListItems);
                    allBookDetailItems.AddRange(books.Select(b => new BookDetailItem()
                    {
                        Id = b.Id,
                        BookId = b.BookId,
                        Title = b.Title,
                        Category = b.Category,
                        InitialLetter = b.InitialLetter,
                        Author = b.Author,
                        Intro = b.Intro,
                        Visited = b.Visited,
                        Rating = b.Rating,
                        RatedBy = b.RatedBy,
                        Chapters = b.Chapters,
                        Finished = b.Finished,
                        IsLatest = b.IsLatest
                    }));
                    Utils.Add2Cache(category.Id, bookListItems);
                    logger.InfoFormat("Done adding category {0}, {1} books added", category.CategoryId, books.Count());
                }

                foreach (var item in allBookListItems)
                {
                    allBookListItemDict.Add(item.Id, item);
                }

                Utils.Add2Cache(Utils.AllBookListItemDictCacheKey, allBookListItemDict);

                Utils.Add2Cache(Utils.AllBookDetailItemsCacheKey, allBookDetailItems);
                foreach (var item in allBookDetailItems)
                {
                    allBookDetailItemDict.Add(item.Id, item);
                }
                Utils.Add2Cache(Utils.AllBookDetailItemDictCacheKey, allBookDetailItemDict);
                var time = DateTime.Now - startDt;
                logger.InfoFormat("Done initialization, total initialization time {0}s", time.TotalSeconds);
            }
        }

    }
}
