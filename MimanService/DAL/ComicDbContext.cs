namespace DAL
{
    using DAL.Entity;
    using System.Data.Entity;
    using System.Linq;
    using System.Collections.Generic;


    /// <summary>
    /// 
    /// </summary>
    public class ComicDbContext : DbContext
    {
        public DbSet<ComicBook> ComicBooks { get; set; }
        public DbSet<Category> Categories { get; set; }
        public DbSet<Chapter> Chapters { get; set; }

        static ComicDbContext()
        {
            //Database.SetInitializer<ComicDbContext>(new DropCreateDatabaseIfModelChanges<ComicDbContext>());
        }

        public ComicDbContext()
        {

        }

        public ComicDbContext(string connectionString)
            : base(connectionString)
        {
            this.Database.CreateIfNotExists();
            if (Categories.Count() == 0)
            {
                Categories.AddRange(Category.DefaultCategories);
                this.SaveChanges();
            }
        }
    }
}
