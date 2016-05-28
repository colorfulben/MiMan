using DAL.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MimanService.Models
{
    public class BookDetailItem
    {
        public int Id { get; set; }
        public int BookId { get; set; }
        public string Title { get; set; }
        public Int64 Category { get; set; }
        public string InitialLetter { get; set; }
        public string Author { get; set; }

        public string Intro { get; set; }

        public int Visited { get; set; }

        public float Rating { get; set; }

        public int RatedBy { get; set; }

        public List<Chapter> Chapters { get; set; }

        public bool Finished { get; set; }

        public bool IsLatest { get; set; }

        public override int GetHashCode()
        {
            return Id;
        }
    }
}