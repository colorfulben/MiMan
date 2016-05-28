
namespace DAL.Entity
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;

    enum DefaultCategoryNames
    {
        萌系 = 1,
        搞笑,
        格斗,
        科幻,
        剧情,
        侦探,
        竞技,
        魔法,
        神鬼,
        校园,
        惊栗,
        厨艺,
        伪娘,
        图片,
        冒险,
        小说 = 19,
        港漫 = 20,
        耽美 = 21,
        经典 = 22,
        欧美 = 23,
        日文 = 24,
        亲情 = 25
    }

    /// <summary>
    /// 
    /// </summary>
    public class Category
    {
        public static List<Category> DefaultCategories { get; private set; }
        [Key]
        public int Id { get; set; }

        public int CategoryId { get; set; }

        public Int64 Mask { get; set; }
        [StringLength(50)]
        public string Name { get; set; }

        static Category()
        {
            DefaultCategories = new List<Category>();
            Int64 mask = 1;
            foreach (var item in Enum.GetValues(typeof(DefaultCategoryNames)))
            {

                DefaultCategories.Add(new Category()
                {
                    CategoryId = (int)item,
                    Mask = mask,
                    Name = item.ToString()
                });
                mask = mask << 1;
            }
        }

    }
}
