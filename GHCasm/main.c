//---------------------------------------------------------------------------
#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#pragma hdrstop

//---------------------------------------------------------------------------

#define STRIP_SPC(s) while(*(s)==' ' || *(s)==9) (s)++
#define IS_SYM(s,v) do{if (*(s)!=(v)) {printf("Expected %c in %s (%d)\n",v,ln,LineN);}s++;}while(0)
#define GET_LITERAL(s,D) do{\
 char *d=(D);\
 while (isalpha(*(s)))\
  {*d++=*(s)++;}\
   *d++=0;\
}while(0)

#define GET_LABEL(s,D) do{\
  char *d=(D);\
  if (isalpha(*(s)))\
   {*d++=*(s)++;}\
  else\
   {printf("Error in label %s (%d)\n",ln,LineN);exit(20);}\
  while(isalnum(*(s)))\
  {*d++=*(s)++;}\
  *d++=0;\
}while(0)

#define GET_OP(s,D) do{\
  char *d=(D); char c;\
  for(;;)\
  {\
        c=*(s);\
        if (c==',' || !c || c==';') break;\
        (s)++;\
        *d++=c;\
  }*d++=0;\
}while(0)

#define IS_EOL(s) (*(s)==',' || !*(s) || *(s)==';')

#pragma argsused
unsigned PASS2=0;
unsigned int PC;
unsigned int RAM;
typedef struct
{
        char name[256];
        unsigned int val;
        unsigned int type;
}LABEL;

LABEL Labels[256];
unsigned int LblTop=0;
unsigned int LineN=0;

LABEL *FindLabel(char *name)
{
        unsigned int i=0;
        while(i<LblTop)
        {
                if (!stricmp(Labels[i].name,name))
                {
                        if (!Labels[i].type && PASS2)
                        {
                               printf("Label %s not defined (%d)\n",name,LineN);
                        }
                        return Labels+i;
                }
                i++;
        }
        strcpy(Labels[i].name,name);
        Labels[i].type=0;
        Labels[i].val=0;
        LblTop++;
        return Labels+i;
}

int Compute(char *s, char *ln)
{
        int val=0;
        int val0;
        char lbl[256];
        char cop=0;
        STRIP_SPC(s);
        while(*s)
        {
                if (isdigit(*s))
                {
                        val0=strtol(s,&s,0);
                }
                else
                {
                        if (isalpha(*s))
                        {
                                LABEL *l;
                                GET_LABEL(s,lbl);
                                l=FindLabel(lbl);
                                if (l->type>999) return l->type;
                                val0=l->val;
                        }
                        else
                        {
                                //Возможно это операция
                                cop=*s++;
                                if (cop==']') goto L_EXIT;
                                goto L_CONT;
                        }
                }
                switch(cop)
                {
                case 0:
                        val=val0;
                        break;
                case '-':
                        val-=val0;
                        break;
                case '+':
                        val+=val0;
                        break;
                default:
                        printf("Unknown arithmetic in %s (%d)\n",ln,LineN);
                        return 0;
                }
                L_CONT:
                STRIP_SPC(s);
        }
        L_EXIT:
        return val & 255;
}

void ParseOp(char *d, char *s, char *ln)
{
        int val;
        if (*s=='[')
        {
                STRIP_SPC(s);
                val=Compute(s,ln);
                switch(val)
                {
                case 1000: sprintf(d,"[A]");break;
                case 1001: sprintf(d,"[B]");break;
                case 1002: sprintf(d,"[C]");break;
                case 1003: sprintf(d,"[D]");break;
                case 1004: sprintf(d,"[E]");break;
                case 1005: sprintf(d,"[F]");break;
                case 1006: sprintf(d,"[G]");break;
                case 1007: sprintf(d,"[H]");break;
                case 1008: sprintf(d,"[PC]");break;
                default: sprintf(d,"%d",val);break;
                }
        }
        else
        {
                STRIP_SPC(s);
                val=Compute(s,ln);
                switch(val)
                {
                case 1000: sprintf(d,"A");break;
                case 1001: sprintf(d,"B");break;
                case 1002: sprintf(d,"C");break;
                case 1003: sprintf(d,"D");break;
                case 1004: sprintf(d,"E");break;
                case 1005: sprintf(d,"F");break;
                case 1006: sprintf(d,"G");break;
                case 1007: sprintf(d,"H");break;
                case 1008: sprintf(d,"PC");break;
                default: sprintf(d,"%d",val);break;
                }
        }
}

int main(int argc, char* argv[])
{
        FILE *fout;
        char ln[256];
        char lbl[256];
        char op[256];
        char op1[256];
        char op2[256];
        char op3[256];
        char op1r[256];
        char op2r[256];
        char op3r[256];
        fout=fopen("outfile","wt");
        FindLabel("A")->type=1000;
        FindLabel("B")->type=1001;
        FindLabel("C")->type=1002;
        FindLabel("D")->type=1003;
        FindLabel("E")->type=1004;
        FindLabel("F")->type=1005;
        FindLabel("G")->type=1006;
        FindLabel("H")->type=1007;
        FindLabel("PC")->type=1008;
        for(PASS2=0;PASS2<2;PASS2++)
        {
                FILE *infile;
                printf("PASS %d.\n",PASS2+1);
                infile=fopen(argv[1],"rt");
                if (!infile)
                {
                        printf("Can't open file %s",argv[1]);
                        exit(10);
                }
                LineN=1;
                RAM=0;
                PC=0;
                do
                {
                        char *rp;
                        fgets(ln,255,infile);
                        if (strlen(ln)) ln[strlen(ln)-1]=0;
                        rp=ln;
                        if (!*rp) goto L_EMPTY;

                        if (*rp!=' ' && *rp!=9)
                        {
                                //Parse label
                                LABEL *l;
                                GET_LABEL(rp,lbl);
                                l=FindLabel(lbl);
                                if (l->type>999) printf("Incorrect register usage %s (%d)\n",ln,LineN);
                                if (!PASS2)
                                {
                                        if (l->type)
                                        {
                                                printf("Label redefined in %s (%d)\n",ln,LineN);
                                        }
                                        l->type=1;
                                        l->val=PC;
                                }
                        }
                        STRIP_SPC(rp);
                        if (!*rp) goto L_EMPTY;
                        if (*rp==';') goto L_EMPTY;
                        GET_LITERAL(rp,op);
                        if (!stricmp(op,"EQU"))
                        {
                                LABEL *l;
                                STRIP_SPC(rp);
                                GET_LABEL(rp,lbl);
                                l=FindLabel(lbl);
                                if (l->type>999) printf("Incorrect register usage %s (%d)\n",ln,LineN);
                                if (!PASS2)
                                {
                                        if (l->type)
                                        {
                                                printf("Label redefined in %s (%d)\n",ln,LineN);
                                        }
                                }
                                l->type=2;
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                //Расчет значения
                                l->val=Compute(op2,ln);
                                if (l->val>999) printf("Incorrect register usage %s (%d)\n",ln,LineN);
                        }
                        if (!stricmp(op,"DS"))
                        {
                                LABEL *l;
                                int x;
                                STRIP_SPC(rp);
                                GET_LABEL(rp,lbl);
                                l=FindLabel(lbl);
                                if (l->type>999) printf("Incorrect register usage %s (%d)\n",ln,LineN);
                                if (!PASS2)
                                {
                                        if (l->type)
                                        {
                                                printf("Label redefined in %s (%d)\n",ln,LineN);
                                        }
                                }
                                l->type=3;
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                //Расчет значения
                                l->val=RAM;
                                x=Compute(op2,ln);
                                if (x>999) printf("Incorrect register usage %s (%d)\n",ln,LineN);
                                else
                                {
                                        RAM+=x; if (RAM>255) printf("Insufficient RAM in %s (%d)\n",ln,LineN);
                                }
                        }
                        if (!stricmp(op,"MOV"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tMOV\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"ADD"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tADD\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"SUB"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tSUB\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"MUL"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tMUL\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"DIV"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tDIV\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"AND"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tAND\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"OR"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tOR\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"XOR"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                if (PASS2) fprintf(fout,"\tXOR\t%s,%s\n",op1r,op2r); PC++;
                        }
                        if (!stricmp(op,"INC"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                ParseOp(op1r,op1,ln);
                                if (PASS2) fprintf(fout,"\tINC\t%s\n",op1r); PC++;
                        }
                        if (!stricmp(op,"DEC"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                ParseOp(op1r,op1,ln);
                                if (PASS2) fprintf(fout,"\tDEC\t%s\n",op1r); PC++;
                        }
                        if (!stricmp(op,"INT"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                ParseOp(op1r,op1,ln);
                                if (PASS2) fprintf(fout,"\tINT\t%s\n",op1r); PC++;
                        }
                        if (!stricmp(op,"HLT"))
                        {
                                STRIP_SPC(rp);
                                if (PASS2) fprintf(fout,"\tHLT\n"); PC++;
                        }
                        if (!stricmp(op,"JGT"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op3);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                ParseOp(op3r,op3,ln);
                                if (PASS2) fprintf(fout,"\tJGT\t%s,%s,%s\n",op1r,op2r,op3r); PC++;
                        }
                        if (!stricmp(op,"JLT"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op3);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                ParseOp(op3r,op3,ln);
                                if (PASS2) fprintf(fout,"\tJLT\t%s,%s,%s\n",op1r,op2r,op3r); PC++;
                        }
                        if (!stricmp(op,"JEQ"))
                        {
                                STRIP_SPC(rp);
                                GET_OP(rp,op1);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op2);
                                IS_SYM(rp,',');
                                STRIP_SPC(rp);
                                GET_OP(rp,op3);
                                ParseOp(op1r,op1,ln);
                                ParseOp(op2r,op2,ln);
                                ParseOp(op3r,op3,ln);
                                if (PASS2) fprintf(fout,"\tJEQ\t%s,%s,%s\n",op1r,op2r,op3r); PC++;
                        }
                        STRIP_SPC(rp);
                        if (*rp && *rp!=';')
                        {
                                printf("Incorrect end of line %s (%d)\n",ln,LineN);
                        }
                        L_EMPTY:
                        LineN++;
                }
                while(!feof(infile));
                fclose(infile);
        }
        fclose(fout);
        return 0;
}
//---------------------------------------------------------------------------
