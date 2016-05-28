using DAL.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DAL
{
    public class ComicBooksRepository : IDisposable
    {
        private const int minVisit = 5000;
        private const float minRate = 8;
        private ComicDbContext ctx;


        public ComicBooksRepository(string connectionString)
        {
            ctx = new ComicDbContext(connectionString);
        }

        public IEnumerable<ComicBook> GetAvailableBooksByCategory(Category category)
        {
            var mask = category.Mask;
            return (from b in this.ctx.ComicBooks.Include("Chapters")
                    where (b.Category & mask) != 0 && b.Chapters.Count != 0 && (b.Visited > minVisit || b.Rating >= minRate)
                    select b).OrderByDescending(b => b.Visited).ToList();
        }

        public IEnumerable<ComicBook> GetUncrawledBooksByCategory(Category category)
        {
            var mask = category.Mask;
            return (from b in this.ctx.ComicBooks.Include("Chapters")
                    where (b.Category & mask) != 0 && b.Chapters.Count == 0 && (b.Visited > minVisit || b.Rating >= minRate)
                    select b).OrderByDescending(b => b.Visited).ToList();
        }

        public IEnumerable<Category> GetCategories()
        {
            return ctx.Categories.ToList();
        }

        public IEnumerable<ComicBook> GetBooksWithPaging(int pageIndex, int pageSize)
        {
            List<ComicBook> books;
            if (pageIndex > 0)
            {
                books = (from b in ctx.ComicBooks.Include("Chapters")
                         where (b.Visited > minVisit || b.Rating >= minRate)
                         orderby b.Id
                         select b).Skip(pageSize * pageIndex).Take(pageSize).ToList();
            }
            else
            {
                books = (from b in ctx.ComicBooks.Include("Chapters")
                         where (b.Visited > minVisit || b.Rating >= minRate)
                         orderby b.Id
                         select b).Take(pageSize).ToList();
            }

            return books;
        }
        public async Task SaveAsync()
        {
            await this.ctx.SaveChangesAsync();
        }


        public void Dispose()
        {
            if (this.ctx != null)
            {
                this.ctx.Dispose();
            }
        }

        ~ComicBooksRepository()
        {
            this.Dispose();
        }
    }
}
