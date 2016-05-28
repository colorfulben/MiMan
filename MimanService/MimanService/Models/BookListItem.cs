using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MimanService.Models
{
    public class BookListItem
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public byte[] Cover { get; set; }
    }
}